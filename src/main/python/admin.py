# -*- coding: utf-8 -*-

import os
import sys

import urllib
import urllib2
import cStringIO
import ConfigParser
import collections
import cookielib
import urlparse
import getpass
import fnmatch
import base64
import random
import string
import csv
import re

try:
  from hashlib import sha1
except ImportError:
  from sha import sha as sha1

try:
  import json
except ImportError:
  import simplejson as json

# ----------------------------------------
# import postgresql driver
# ----------------------------------------

try:
  import psycopg2
  import psycopg2.extensions
except ImportError:
  print >>sys.stderr, "Please install python-psycopg2 with yum package manager."
  sys.exit(1)

if psycopg2.__version__ < "2.0":
  print >>sys.stderr, "Please install psycopg2 version 2.0 or later."
  sys.exit(1)

psycopg2.extensions.register_type(psycopg2.extensions.UNICODE)
psycopg2.extensions.register_type(psycopg2.extensions.UNICODEARRAY)

# ----------------------------------------
# global variables
# ----------------------------------------

codedepot_url = "http://localhost:8080/codedepot/"
properties_folder = "/opt/codedepot/web/WEB-INF/classes"
admin_cfg_path = "/opt/codedepot/etc/admin.cfg"

account_keys = ['username', 'email', 'role', 'lang', 'active', 'note']
project_keys = ['title', 'description', 'license', 'site_url', 'download_url',
                'src_type', 'src_path', 'scm_user', 'scm_pass',
                'crontab', 'ignores', 'admin', 'restricted']

account_roles = {
  0: u"一般ユーザ",
  1: u"プロジェクト管理者",
  2: u"システム管理者"
}
lang_names = {
  "java": "Java",
  "C": "C/C++",
  "csharp": "C#",
  "vb.net": "VB.NET"
}
src_types = {
  "local": "local",
  "svn": "Subversion",
  "cvs": "CVS",
  "git": "Git",
  "jazz": "Jazz"
}

username_pattern = re.compile("^[0-9A-Za-z\\-\\._@]+$")
email_pattern = re.compile("^[\\w\\.\\-]+@(?:[\\w\\-]+\\.)+[\\w\\-]+$")
projname_pattern = re.compile("^[0-9A-Za-z\\.\\-_]+$")
license_pattern = re.compile("^[0-9A-Za-z\\.\\-\\+_/]+$")

if 'WEBAPP_ROOT' in os.environ:
  properties_folder = os.path.join(os.environ['WEBAPP_ROOT'], 'WEB-INF/classes')

# ----------------------------------------
# load jdbc properties files
# ----------------------------------------

def load_jdbc_properties(filename):
  '''jdbc プロパティファイルの読込み'''
  ini = cStringIO.StringIO()
  ini.write('[jdbc]\n')
  try:
    fp = open(filename, 'r')
  except IOError:
    return None

  try:
    ini.write(fp.read())
  finally:
    fp.close()

  try:
    ini.seek(0, os.SEEK_SET)
  except AttributeError:
    ini.seek(0, 0)

  config = ConfigParser.ConfigParser()
  try:
    config.readfp(ini)
  except IOError:
    return None

  opts = dict(config.items('jdbc'))
  if 'url' not in opts:
    return None

  if 'url' in opts and opts['url'].startswith("jdbc:"):
    try:
      url = opts['url'][4:].replace(":postgresql:", "http:")
      info = urlparse.urlparse(url)
      if isinstance(info, tuple):
        if info[1].find(":") < 0:
          opts['hostname'] = info[2]
          opts['port'] = None
        else:
          (host, port) = info[1].split(":", 1)
          opts['hostname'] = host
          opts['port'] = port
        if info[2].startswith("/"):
          opts['path'] = info[3][1:]
        else:
          opts['path'] = info[3]
      else:
        opts['hostname'] = info.hostname
        opts['port'] = info.port
        if info.path.startswith("/"):
          opts['path'] = info.path[1:]
        else:
          opts['path'] = info.path
    except:
      pass

  if 'hostname' not in opts:
    opts['hostname'] = "localhost"

  if 'port' not in opts or opts['port'] is None:
    if 'PGPORT' in os.environ:
      opts['port'] = os.environ['PGPORT']
    else:
      opts['port'] = "5432"

  if 'path' not in opts or opts['path'] == "":
    opts['path'] = "codedepot"

  return dict(opts)

# ----------------------------------------
# Database class
# ----------------------------------------

class CodeDepotDatabase:
  def __init__(self, host, port, dbname, username, password):
    '''オブジェクトの初期化。'''
    self.session = None
    self.db_host = host
    self.db_port = port
    self.db_name = dbname
    self.db_user = username
    self.db_pass = password

  def connect(self):
    '''データベースに接続する。'''
    if self.session is None:
      self.session = psycopg2.connect(
        host=self.db_host,
        port=self.db_port,
        database=self.db_name,
        user=self.db_user,
        password=self.db_pass)

  def disconnect(self):
    '''データベースの接続を切断する。'''
    if self.session is not None:
      self.session.close()
      self.session = None

  def query(self, sql, params=None):
    '''検索 SQL を実行し、検索結果を返す。'''
    self.connect()
    cursor = self.session.cursor()
    try:
      try:
        cursor.execute(sql, params)
        keys = [ e[0] for e in cursor.description ]
        rows = [ dict(zip(keys, e)) for e in cursor ]
        return rows
      except IOError:
        return None
    finally:
      cursor.close()

  def update(self, sql, params=None):
    '''更新 SQL を実行し、更新件数を返す。'''
    self.connect()
    cursor = self.session.cursor()
    try:
      try:
        cursor.execute(sql, params)
        self.session.commit()
        return cursor.rowcount
      except IOError:
        self.session.rollback()
        return None
    finally:
      cursor.close()

  def get_user_names(self):
    '''ユーザ名の一覧を取得する。'''
    sql = '''
        SELECT username FROM member
            WHERE del_flag = FALSE
            ORDER BY username
    '''
    result = self.query(sql)
    return [ e['username'] for e in result ]

  def get_all_users(self):
    '''全ユーザの情報を取得する。'''
    sql = '''
        SELECT id, username, email, role, active, def_lang AS lang, note
            FROM member WHERE del_flag = FALSE
            ORDER BY username
    '''
    return self.query(sql)

  def get_user_by_id(self, uid):
    '''指定したユーザの情報を取得する。'''
    sql = '''
        SELECT id, username, email, role, active, def_lang AS lang, note
            FROM member WHERE del_flag = FALSE AND id = %(id)s
    '''
    r = self.query(sql, {'id': str(uid)})
    if r:
      return r[0]
    else:
      return None

  def get_user_by_name(self, name):
    '''指定したユーザの情報を取得する。'''
    sql = '''
        SELECT id, username, email, role, active, def_lang AS lang, note
            FROM member WHERE del_flag = FALSE AND username = %(name)s
    '''
    r = self.query(sql, {'name': name})
    if r:
      return r[0]
    else:
      return None

  def add_user(self, name, email, role, lang, active, note):
    '''ユーザを追加する。'''
    sql = '''
        INSERT INTO member(username, email, role,
                           def_lang, active, note,
                           cuserid, ctime, muserid, mtime)
            VALUES(%(name)s, %(email)s, %(role)s,
                   %(lang)s, %(active)s, %(note)s,
                  1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP)
    '''
    params = {'name': name, 'email': email,
              'role': role, 'lang': lang,
              'active': active, 'note': note}
    return self.update(sql, params)

  def mod_user(self, name, attr, value):
    '''ユーザを変更する。'''
    attr_map = {"lang": "def_lang", "comment": "note"}
    if attr in attr_map:
      attr = attr_map[attr]

    sql = ''' UPDATE member SET {{attr}} = %(value)s,
                   muserid = 1, mtime = CURRENT_TIMESTAMP
                   WHERE del_flag = FALSE AND username = %(name)s
    '''
    sql = sql.replace("{{attr}}", attr)
    return self.update(sql, {"name": name, "value": value})

  def delete_user(self, name):
    '''指定したユーザを削除する。'''
    sql = '''
        UPDATE member SET del_flag=TRUE, mtime = CURRENT_TIMESTAMP
            WHERE del_flag = FALSE AND username = %(name)s
    '''
    return self.update(sql, {'name': name})

  def change_password(self, name, passwd):
    '''指定したユーザのパスワードを更新する。'''
    sql = '''
        UPDATE member SET password = %(pass)s, pwd_mtime = CURRENT_TIMESTAMP
            WHERE del_flag = FALSE AND username = %(name)s
    '''
    return self.update(sql, {'name': name, 'pass': passwd})

  def get_project_names(self):
    '''プロジェクト名の一覧を取得する。'''
    sql = '''
        SELECT title FROM project
            WHERE del_flag = FALSE
            ORDER BY title
    '''
    result = self.query(sql)
    return [ e['title'] for e in result ]

  def get_all_projects(self):
    '''全プロジェクトの一覧を取得する。'''
    sql = '''
        SELECT id,name,title,description,license,
            site_url,download_url,restricted,
            src_type,src_path,scm_user,scm_pass,
            crontab,admin,ignores,indexed_at
            FROM project WHERE del_flag = FALSE
            ORDER BY title
    '''
    return self.query(sql)

  def get_project_by_id(self, uid):
    '''プロジェクトの情報を取得する。'''
    sql = '''
        SELECT id,name,title,description,license,
            site_url,download_url,restricted,
            src_type,src_path,scm_user,scm_pass,
            crontab,admin,ignores,indexed_at
            FROM project WHERE del_flag = FALSE AND id = %(id)d
    '''
    r = self.query(sql, {'id': str(uid)})
    if r:
      return r[0]
    else:
      return None

  def get_project_by_name(self, name):
    '''プロジェクトの情報を取得する。'''
    sql = '''
        SELECT id,name,title,description,license,
            site_url,download_url,restricted,
            src_type,src_path,scm_user,scm_pass,
            crontab,admin,ignores,indexed_at
            FROM project WHERE del_flag = FALSE AND title = %(name)s
    '''
    r = self.query(sql, {'name': name})
    if r:
      return r[0]
    else:
      return None

  def add_project(self, title, description, license, site_url, download_url,
                  src_type, src_path, scm_user, scm_pass,
                  crontab, ignores, admin, restricted):
    '''プロジェクトを追加する。'''
    name = self.get_unique_name()
    if name is None:
      return 0
    sql = '''
        INSERT INTO project(name, title, description, license,
                            site_url, download_url, restricted,
                            src_type, src_path, scm_user, scm_pass,
                            crontab, admin, ignores,
			    cuserid, ctime, muserid, mtime)
            VALUES(%(name)s, %(title)s, %(description)s, %(license)s,
                   %(site_url)s, %(download_url)s, %(restricted)s,
                   %(src_type)s, %(src_path)s, %(scm_user)s, %(scm_pass)s,
                   %(crontab)s, %(admin)s, %(ignores)s,
                   1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP)
    '''
    params = {'name': name, 'title': title,
              'description': description, 'license': license,
              'site_url': site_url, 'download_url': download_url,
              'restricted': restricted, 'ignores': ignores,
              'src_type': src_type, 'src_path': src_path,
              'scm_user': scm_user, 'scm_pass': scm_pass,
              'crontab': crontab, 'admin': admin}
    return self.update(sql, params)

  def mod_project(self, name, attr, value):
    '''プロジェクトを変更する。'''
    if attr in ['scm_user', 'scm_pass', 'crontab',
                'site_url', 'download_url', 'admin']:
      sql = '''
          UPDATE project SET {{attr}} = %(value)s
                     WHERE del_flag = FALSE AND title = %(name)s
      '''
    else:
      sql = '''
          UPDATE project SET {{attr}} = %(value)s,
                     muserid = 1, mtime = CURRENT_TIMESTAMP
                     WHERE del_flag = FALSE AND title = %(name)s
      '''
    sql = sql.replace("{{attr}}", attr)
    return self.update(sql, {"name": name, "value": value})

  def delete_project(self, name):
    '''プロジェクトを削除する。'''
    sql = '''
        UPDATE project SET del_flag=TRUE, mtime = CURRENT_TIMESTAMP
            WHERE del_flag = FALSE AND title = %(name)s
    '''
    return self.update(sql, {'name': name})

  def get_permit_users(self, name):
    '''プロジェクトのアクセス許可ユーザの一覧を取得する。'''
    sql = '''
        SELECT a.username FROM member AS a, project AS p, permit AS t
            WHERE p.del_flag = FALSE AND p.title = %(name)s AND
                  p.name = t.project AND t.mid = a.id AND
                  a.del_flag = FALSE ORDER BY a.username
    '''
    result = self.query(sql, {"name": name})
    return [ e['username'] for e in result ]

  def set_permit_users(self, name, users):
    '''プロジェクトのアクセス許可ユーザの一覧を設定する。'''
    sql = '''
        DELETE FROM permit where project IN
          (SELECT name FROM Project WHERE
               del_flag = FALSE AND title = %(name)s)
    '''
    self.update(sql, {"name": name})

    sql = '''
        INSERT INTO permit(project, mid)
          SELECT p.name, a.id FROM project AS p, member AS a
              WHERE p.del_flag = FALSE AND p.title = %(name)s AND
                    a.del_flag = FALSE AND a.username = %(user)s
    '''
    for e in users:
      self.update(sql, {"name": name, 'user': e})

  def get_unique_name(self):
    '''ユニークなプロジェクト識別子を取得する。'''
    letters = string.lowercase + string.digits
    sql = '''
      SELECT 1 FROM project WHERE name = %(name)s
    '''
    for retry in xrange(0, 100):
      name = "_" + "".join(random.choice(letters) for i in xrange(0, 12))
      found = self.query(sql, {"name": name})
      if not found:
        return name
    return None

  def get_file_count(self):
    '''プロジェクトの登録ファイル数を取得する。'''
    sql = '''
        SELECT p.title, s.lang, COUNT(1) AS count, SUM(s.lines) as lines
            FROM source AS s, project AS p
            WHERE p.del_flag = FALSE AND p.name = s.project
            GROUP BY p.title, s.lang ORDER BY p.title
    '''
    return self.query(sql)

  def get_all_logs(self):
    '''全てのログ情報を取得する。'''
    sql = '''SELECT b.stime, b.etime, b.period, p.name, p.title, b.msg, b.status
             FROM batchlog AS b
             LEFT JOIN project AS p ON b.project = p.name
             WHERE p.del_flag = false
             ORDER BY b.etime DESC
    '''
    return self.query(sql)

  def delete_all_logs(self):
    '''全てのログ情報を削除する。'''
    sql = '''DELETE FROM batchlog'''
    return self.update(sql)

# ----------------------------------------
# Client class
# ----------------------------------------

class CodeDepotClient:
  def __init__(self, url):
    '''urllib2 の初期化を行い、ベースURL を設定する。'''
    proxy = urllib2.ProxyHandler({})
    cookie = urllib2.HTTPCookieProcessor(cookielib.CookieJar())
    opener = urllib2.build_opener(proxy, cookie)
    urllib2.install_opener(opener)
    self.base = url
    if not url.endswith("/"):
      self.base = url + "/"

  def call(self, url, data=None, user=None, passwd=None):
    '''指定された URL を呼び出し、JSONを返す。'''
    req = urllib2.Request(self.base + url)
    if user is not None:
      auth = '%s:%s' % (user, passwd)
      cred = base64.encodestring(auth).rstrip()
      header = "Basic %s" % cred
      req.add_header("Authorization", header)
    if data:
      params = urllib.urlencode(data)
    else:
      params = None
    try:
      handler = urllib2.urlopen(req, params)
    except IOError:
      return None

    reply = handler.read()
    obj = json.loads(reply)
    handler.close()
    return obj

  def login(self, user=None, passwd=None):
    '''ログインを行い、セッションを開始する。'''
    return self.call('admin/account/login?json=true', None, user, passwd)

  def update_project(self, title):
    '''指定したプロジェクトのインデックスを更新する。'''
    params = {"method": "doRun", "title": title}
    return self.call('admin/project/projlist', params)

  def reload_project(self):
    params = {"method": "doReload"}
    return self.call('admin/project/projlist', params)

  def delete_project(self, name):
    '''指定したプロジェクトを削除する。'''
    params = {"name": name}
    return self.call('admin/project/projDelete', params)

# ----------------------------------------
# Admin command for account
# ----------------------------------------

class AccountAdmin:
  def __init__(self, db):
    self.db = db

  def do_command(self, args):
    '''ユーザ管理のためのコマンドを実行する。'''

    usage = "user {list|show|add|mod|del|passwd|export|import} [...]"
    usage += "\n  * list    -- show list of user account."
    usage += "\n  * show    -- show attributes of user account."
    usage += "\n  * add     -- add new user account."
    usage += "\n  * mod     -- change user attributes."
    usage += "\n  * del     -- delete user accounts."
    usage += "\n  * passwd  -- change user password."
    usage += "\n  * export  -- export user lists to cvs file."
    usage += "\n  * import  -- import user lists from cvs file."

    if len(args) < 1:
      show_usage(usage)
      return 1

    command, options = args[0], args[1:]

    if command == "list":
      return self.do_list(options)
    if command == "show":
      return self.do_show(options)
    if command == "add":
      return self.do_add(options)
    if command == "mod":
      return self.do_mod(options)
    if command == "del":
      return self.do_del(options)
    if command == "passwd":
      return self.do_passwd(options)
    if command == "export":
      return self.do_export(options)
    if command == "import":
      return self.do_import(options)

    show_usage(usage)
    return 1

  def do_list(self, args):
    '''ユーザの一覧を表示する。'''
    for e in self.db.get_user_names():
      show_result(e)

  def do_show(self, args):
    '''ユーザの詳細を表示する。'''

    usage = "user show <username> ..."
    if len(args) < 1:
      show_usage(usage)
      return 1

    users = []
    for e in args:
      info = self.db.get_user_by_name(e)
      if info is None:
        show_error("user '%s' does not exist." % e)
        return 2
      users.append(info)

    for e in users:
      group = self.get_role_name(e['role'])
      active = self.get_active_name(e['active'])
      lang = self.get_lang_name(e['lang'])

      show_result(u"login name:\t%s" % e['username'])
      show_result(u"email address:\t%s" % (e['email'] or ""))
      show_result(u"user role:\t%d (%s)" % (e['role'], group))
      show_result(u"active flag:\t%d (%s)" % (e['active'], active))
      show_result(u"default lang:\t%s" % (lang or ""))
      show_result(u"admin note:\t%s" % (e['note'] or ""))

    return 0

  def do_add(self, args):
    '''ユーザを追加する。'''

    usage = "user add <username> <email> <role> [<lang>] [<note>]"
    roles = sorted(account_roles.items(), key=lambda x: x[0])
    usage += "\n  <role>: " + ", ".join("%d:%s" % e for e in roles)
    langs = sorted(lang_names.items(), key=lambda x: x[0])
    usage += "\n  <lang>: " + ", ".join("%s:%s" % e for e in langs)

    if len(args) < 3:
      show_usage(usage)
      return 1

    username, email, role = args[0:3]

    if len(args) > 3:
      lang = args[3]
    else:
      lang = "java"

    if len(args) > 4:
      note = args[4]
    else:
      note = ""

    if not self.check_value("username", username):
      show_error("invalid username.")
      return 2

    if not self.check_value("email", email):
      show_error("invalid email.")
      return 2

    if not self.check_value("role", role):
      show_error("invalid role.")
      return 2

    if not self.check_value("lang", lang):
      show_error("invalid lang.")
      return 2

    if not self.check_value("note", note):
      show_error("invalid note.")
      return 2

    info = self.db.get_user_by_name(username)
    if info is not None:
      show_error("user '%s' already exists." % username)
      return 2

    email = self.get_db_value("email", email)
    role = self.get_db_value("role", role)
    lang = self.get_db_value("lang", lang)
    note = self.get_db_value("note", note)

    self.db.add_user(username, email, role, lang, True, note)
    show_info("add user '%s'." % username)

    return 0

  def do_mod(self, args):
    '''ユーザを更新する。'''

    padding = " " * 25
    usage = "user mod <username> [email=<email>]"
    usage += "\n%s[role=<role>]" % padding
    usage += "\n%s[lang=<lang>]" % padding
    usage += "\n%s[note=<note>]" % padding
    usage += "\n%s[active={0|1}]" % padding
    roles = sorted(account_roles.items(), key=lambda x: x[0])
    usage += "\n  <role>: " + ", ".join("%d:%s" % e for e in roles)
    langs = sorted(lang_names.items(), key=lambda x: x[0])
    usage += "\n  <lang>: " + ", ".join("%s:%s" % e for e in langs)

    if len(args) < 2:
      show_usage(usage)
      return 1

    username = args[0]
    params = args[1:]

    for p in params:
      item = p.split("=", 1)
      if len(item) == 2 and item[0] in ["email", "role", "lang", "note", "active"]:
        continue
      show_usage(usage)
      return 1

    info = self.db.get_user_by_name(username)
    if info is None:
      show_error("user '%s' does not exist." % username)
      return 2

    for p in params:
      k, v = p.split("=", 1)
      if k == "email" and not self.check_value(k, v):
        show_error("invalid email.")
        return 2
      if k == "role" and not self.check_value(k, v):
        show_error("invalid role.")
        return 2
      if k == "lang" and not self.check_value(k, v):
        show_error("invalid lang.")
        return 2
      if k == "active" and not self.check_value(k, v):
        show_error("invalid active value.")
        return 2
      if k == "note" and not self.check_value(k, v):
        show_error("invalid note.")
        return 2

    for p in params:
      k, v = p.split("=", 1)
      value = self.get_db_value(k, v)
      self.db.mod_user(username, k, value)

    show_info("change user '%s'." % username)
    return 0

  def do_del(self, args):
    '''ユーザを削除する。'''

    usage = "user del <username> ..."
    if len(args) < 1:
      show_usage(usage)
      return 1

    users = []
    for e in args:
      info = self.db.get_user_by_name(e)
      if info is None:
        show_error("user '%s' does not exist." % e)
        return 2
      users.append(info)

    for e in users:
      self.db.delete_user(e['username'])
      show_info("delete user '%s'." % e['username'])

    return 0

  def do_passwd(self, args):
    '''ユーザのパスワードを変更する。'''

    usage = "user passwd <username> <password>"
    if len(args) != 2:
      show_usage(usage)
      return 1

    username, password = args
    digest = sha1(password).digest()
    passwd = base64.encodestring(digest).rstrip()

    self.db.change_password(username, passwd)
    show_info("change password for '%s'." % username)

    return 0

  def do_export(self, args):
    '''ユーザの一覧を CSV 形式で出力する。'''

    usage = "user export <filename>"
    if len(args) != 1:
      show_usage(usage)
      return 1

    try:
      if args[0] != '-':
        fd = open(args[0], 'wb')
      else:
        fd = sys.stdout
    except IOError:
      show_error("cannot open '%s'." % args[0])
      return 2

    try:
      writer = csv.writer(fd)

      row = [ "#" + account_keys[0] ]
      row += [ account_keys[i] for i in xrange(1, len(account_keys)) ]
      writer.writerow(row)

      for e in self.db.get_all_users():
        values = [ self.get_csv_value(k, e[k]) for k in account_keys ]
        writer.writerow(values)
    finally:
      fd.close()

    return 0

  def do_import(self, args):
    '''ユーザの一覧を CSV 形式で取り込む。'''

    usage = "user import <filename>"
    if len(args) != 1:
      show_usage(usage)
      return 1

    try:
      if args[0] != '-':
        fd = open(args[0], 'rb')
      else:
        fd = sys.stdin
    except IOError:
      show_error("cannot open '%s'." % args[0])
      return 2

    try:
      reader = csv.reader(fd)

      new_users = []
      old_users = []

      lineno = 0
      for row in reader:
        lineno += 1
        if lineno == 1 and row[0][0] == "#":
          continue

        if len(row) == 0:
          continue

        if len(row) < 6:
          show_error("too few column in line %d." % lineno)
          return 2

        if len(row) > 6:
          show_error("too many column in line %d." % lineno)
          return 2

        username, email, role, lang, active, note = row

        if not self.check_value("username", username):
          show_error("invalid username in line %d." % lineno)
          return 2

        if not self.check_value("email", email):
          show_error("invalid email in line %d." % lineno)
          return 2

        if not self.check_value("role", role):
          show_error("invalid role in line %d." % lineno)
          return 2

        if not self.check_value("lang", lang):
          show_error("invalid lang in line %d." % lineno)
          return 2

        if not self.check_value("active", active):
          show_error("invalid active value in line %d." % lineno)
          return 2

        if not self.check_value("note", note):
          show_error("invalid note.")
          return 2

        new_users.append(row)
    finally:
      fd.close()

    for e in self.db.get_all_users():
      row = [ self.get_csv_value(k, e[k]) for k in account_keys ]
      old_users.append(row)

    old_map = dict((e[0], e) for e in old_users)
    new_map = dict((e[0], e) for e in new_users)

    rows_add = [ new_map[e] for e in new_map if e not in old_map ]
    rows_del = [ old_map[e] for e in old_map if e not in new_map ]
    rows_mod = [ new_map[e] for e in new_map \
                 if e in old_map and old_map[e] != new_map[e] ]

    for e in rows_add:
      username = e[0]
      for i in xrange(1, len(account_keys)):
        e[i] = self.get_db_value(account_keys[i], e[i])
      self.db.add_user(username, e[1], e[2], e[3], e[4], e[5])
      show_info("add user '%s'." % username)

    for e in rows_mod:
      username = e[0]
      (old_row, new_row) = (old_map[username], new_map[username])
      for i in xrange(1, len(account_keys)):
        if old_row[i] != new_row[i]:
          value = self.get_db_value(account_keys[i], new_row[i])
          self.db.mod_user(username, account_keys[i], value)
      show_info("modify user '%s'." % username)

    for e in rows_del:
      username = e[0]
      self.db.delete_user(e[0])
      show_info("delete user '%s'." % username)

    return 0

  def check_value(self, name, value):
    '''ユーザの入力値をチェックする。'''
    if name == 'username':
      if not username_pattern.match(value):
        return False
      return True

    if name == 'email':
      if len(value) > 0 and not email_pattern.match(value):
        return False
      return True

    if name == 'role':
      roles = [ str(e) for e in account_roles ]
      return value in roles

    if name == 'lang':
      return value in lang_names

    if name == 'active':
      return value in ["0", "1"]

    if name == 'note':
      try:
        unicode(value, 'utf-8')
        return True
      except UnicodeError:
        return False

    return False

  def get_role_name(self, role):
    '''ユーザの権限を取得する。'''
    if role in account_roles:
      return account_roles[role]
    else:
      return "-"

  def get_active_name(self, active):
    '''ユーザのログイン可否を取得する。'''
    if active:
      return u"有効"
    else:
      return u"無効"

  def get_lang_name(self, lang):
    '''ユーザの検索言語を取得する。'''
    if lang in lang_names:
      return lang_names[lang]
    else:
      return lang

  def get_csv_value(self, name, value):
    '''csv のカラム値を出力する。'''
    if value is None:
      return ""
    if isinstance(value, unicode):
      return value.encode("utf-8")
    if isinstance(value, bool):
      return str(int(value))
    return str(value)

  def get_db_value(self, name, value):
    '''db のカラム値を出力する。'''
    if name == "role":
      return int(value)
    if name == "active":
      return bool(int(value))
    if name == "note":
      return unicode(value, "utf-8")
    return value

# ----------------------------------------
# Admin command for project
# ----------------------------------------

class ProjectAdmin:
  def __init__(self, db):
    self.db = db

  def do_command(self, args):
    '''プロジェクト管理のためのコマンドを実行する。'''

    usage = "project {list|show|add|mod|del|update|export|import|files|lines} [...]"
    usage += "\n  * list    -- show list of registered projects."
    usage += "\n  * show    -- show attributes of registered project."
    usage += "\n  * add     -- add new user project."
    usage += "\n  * mod     -- change project attributes."
    usage += "\n  * del     -- delete registered projects."
    usage += "\n  * update  -- update search index for target projects."
    usage += "\n  * reload  -- re-schedule cron job for all projects."
    usage += "\n  * export  -- export project lists to cvs file."
    usage += "\n  * import  -- import project lists from cvs file."
    usage += "\n  * files   -- show number of files in registered project."
    usage += "\n  * lines   -- show number of lines in registered project."

    if len(args) < 1:
      show_usage(usage)
      return 1

    command, options = args[0], args[1:]

    if command == "list":
      return self.do_list(options)
    if command == "show":
      return self.do_show(options)
    if command == "add":
      return self.do_add(options)
    if command == "mod":
      return self.do_mod(options)
    if command == "del":
      return self.do_del(options)
    if command == "update":
      return self.do_update(options)
    if command == "reload":
      return self.do_reload(options)
    if command == "export":
      return self.do_export(options)
    if command == "import":
      return self.do_import(options)
    if command == "files":
      return self.do_files(options)
    if command == "lines":
      return self.do_lines(options)

    show_usage(usage)
    return 1

  def do_list(self, args):
    '''プロジェクトの一覧を表示する。'''
    for e in self.db.get_project_names():
      print e
    return 0

  def do_show(self, args):
    '''プロジェクトの詳細を表示する。'''

    usage = "project show <project-name> ..."
    if len(args) < 1:
      show_usage(usage)
      return 1

    projects = []
    for e in args:
      info = self.db.get_project_by_name(e)
      if info is None:
        show_error("project '%s' does not exist." % e)
        return 2
      projects.append(info)

    for e in projects:
      stype = self.get_type_name(e['src_type'])
      spath = self.get_src_path(e['src_path'])
      restricted = self.get_restricted_name(e['restricted'])
      admin = self.get_admin_name(e['admin'])
      site_url = self.get_url_name(e['site_url'])
      download_url = self.get_url_name(e['download_url'])
      permit = self.db.get_permit_users(e['title'])

      show_result(u"project name:\t%s" % e['title'])
      show_result(u"site url:\t%s" % (site_url or ""))
      show_result(u"download url:\t%s" % (download_url or ""))
      show_result(u"source type:\t%s" % stype)
      show_result(u"source path:\t%s" % spath)
      show_result(u"scm username:\t%s" % (e['scm_user'] or ""))
      show_result(u"index schedule:\t%s" % (e['crontab'] or ""))
      show_result(u"ignore files:\t%s" % (e['ignores'] or ""))
      show_result(u"admin user:\t%s" % (admin or ""))
      show_result(u"access control:\t%s" % restricted)
      show_result(u"permit users:\t%s" % " ".join(permit))
      show_result(u"index updated:\t%s" % (e['indexed_at'] or ""))

    return 0

  def do_add(self, args):
    '''プロジェクトを追加する。'''

    padding = " " * 28
    usage = "project add <project-name> '<description>' '<license>'"
    usage += "\n%s'<site url>' '<download url>'" % padding
    usage += "\n%s'<source type>' '<source path>'" % padding
    usage += "\n%s'[<scm username>]' '[<scm password>]'" % padding
    usage += "\n%s'[<index schedule>]' ['<ignores ...>']" % padding
    types = sorted(src_types.items(), key=lambda x: x[0])
    usage += "\n  <source type>: " + ", ".join("%s:%s" % e for e in types)

    if len(args) < 7:
      show_usage(usage)
      return 1

    (projname, description, license, site_url, download_url,
     src_type, src_path) = args[0:7]

    if len(args) > 7:
      scm_user = args[7]
    else:
      scm_user = ""

    if len(args) > 8:
      scm_pass = args[8]
    else:
      scm_pass = ""

    if len(args) > 9:
      crontab = args[9]
    else:
      crontab = ""

    if len(args) > 10:
      ignores = args[10]
    else:
      ignores = ""

    if not self.check_value("title", projname):
      show_error("invalid project-name.")
      return 2

    if not self.check_value("description", description):
      show_error("invalid description.")
      return 2

    if not self.check_value("license", license):
      show_error("invalid license.")
      return 2

    if not self.check_value("src_type", src_type):
      show_error("invalid source type.")
      return 2

    if not self.check_value("src_path", src_path):
      show_error("invalid source path.")
      return 2

    if not self.check_value("crontab", crontab):
      show_error("invalid index schedule.")
      return 2

    description = self.get_db_value("description", description)
    license = self.get_db_value("license", license)
    site_url = self.get_db_value("site_url", site_url)
    download_url = self.get_db_value("download_url", download_url)
    src_type = self.get_db_value("src_type", src_type)
    src_path = self.get_db_value("src_path", src_path)
    scm_user = self.get_db_value("scm_user", scm_user)
    scm_pass = self.get_db_value("scm_user", scm_pass)
    crontab = self.get_db_value("crontab", crontab)
    ignores = self.get_db_value("crontab", ignores)

    if crontab:
      do_reload = True
    else:
      do_reload = False

    if do_reload:
      client = self.get_client()
      if client is None:
        show_error("login failed.")
        return 3

    self.db.add_project(projname, description, license,
                        site_url, download_url,
                        src_type, src_path,
                        scm_user, scm_pass,
                        crontab, ignores,
                        None, False)
    show_info("add project '%s'." % projname)

    if do_reload:
      client.reload_project()

    return 0

  def do_mod(self, args):
    '''プロジェクトを変更する。'''

    padding = " " * 28
    usage = "project mod <project-name> [description='<description>']"
    usage += "\n%s[license='<license>']" % padding
    usage += "\n%s[site_url='<site url>']" % padding
    usage += "\n%s[download_url='<download url>']" % padding
    usage += "\n%s[src_type='<source type>']" % padding
    usage += "\n%s[src_path='<source path>']" % padding
    usage += "\n%s[scm_user='<scm username>']" % padding
    usage += "\n%s[scm_pass='<scm password>']" % padding
    usage += "\n%s[crontab='<index schedule>']" % padding
    usage +=" \n%s[admin='<admin>']" % padding
    usage += "\n%s[restricted={0|1}]" % padding
    usage += "\n%s[permit='<users ...>']" % padding
    usage += "\n%s[ignores='<ignores>'" % padding
    types = sorted(src_types.items(), key=lambda x: x[0])
    usage += "\n  <source type>: " + ", ".join("%s:%s" % e for e in types)

    if len(args) < 2:
      show_usage(usage)
      return 1

    projname = args[0]
    params = args[1:]

    keys = ["description", "license", "site_url", "download_url",
            "src_type", "src_path", "scm_user", "scm_pass",
            "crontab", "admin", "restricted", "permit", "ignores"]

    for p in params:
      item = p.split("=", 1)
      if len(item) == 2 and item[0] in keys:
        continue
      show_usage(usage)
      return 1

    all = self.db.get_project_names()
    projects = [ e for e in all if fnmatch.fnmatch(e, projname) ]

    if len(projects) == 0:
      show_error("project '%s' does not exist." % projname)
      return 2

    for p in params:
      k, v = p.split("=", 1)
      if k == "description" and not self.check_value(k, v):
        show_error("invalid description.")
        return 2
      if k == "license" and not self.check_value(k, v):
        show_error("invalid license.")
        return 2
      if k == "src_type" and not self.check_value(k, v):
        show_error("invalid source type.")
        return 2
      if k == "src_path" and not self.check_value(k, v):
        show_error("invalid source path.")
        return 2
      if k == "crontab" and not self.check_value(k, v):
        show_error("invalid index schedule.")
        return 2
      if k == "restricted" and not self.check_value(k, v):
        show_error("invalid restricted value.")
        return 2
      if k == "admin" and len(v) > 0:
        info = self.db.get_user_by_name(v)
        if info is None:
          show_error("user '%s' does not exist." % v)
          return 2
      if k == "permit":
        for u in v.split():
          info = self.db.get_user_by_name(u)
          if info is None:
            show_error("user '%s' does not exist." % u)
            return 2

    do_reload = False
    for p in params:
      k, v = p.split("=", 1)
      if k == 'crontab':
        do_reload = True

    if do_reload:
      client = self.get_client()
      if client is None:
        show_error("login failed.")
        return 3

    for e in projects:
      for p in params:
        k, v = p.split("=", 1)
        if k != 'permit':
          value = self.get_db_value(k, v)
          self.db.mod_project(e, k, value)
        else:
          self.db.set_permit_users(e, v.split())

      show_info("change project '%s'." % e)

    if do_reload:
      client.reload_project()

    return 0

  def do_del(self, args):
    '''プロジェクトを削除する。'''

    usage = "project del <project-name> ..."
    if len(args) < 1:
      show_usage(usage)
      return 1

    projects = []
    for e in args:
      info = self.db.get_project_by_name(e)
      if info is None:
        show_error("project '%s' does not exist." % e)
        return 2
      projects.append(info)

    client = self.get_client()
    if client is None:
      show_error("login failed.")
      return 3

    for e in projects:
      client.delete_project(e['name'])
      show_info("delete project '%s'." % e['title'])

    return 0

  def do_update(self, args):
    '''プロジェクトのインデックスを更新する。'''

    usage = "project update <project-name> ..."
    if len(args) < 1:
      show_usage(usage)
      return 1

    projects = []
    for e in args:
      info = self.db.get_project_by_name(e)
      if info is None:
        show_error("project '%s' does not exist." % e)
        return 2
      projects.append(info)

    client = self.get_client()
    if client is None:
      show_error("login failed.")
      return 3

    for e in projects:
      client.update_project(e['title'])
      show_info("update index for project '%s'." % e['title'])

    return 0

  def do_reload(self, args):
    '''プロジェクトの自動更新を再設定する。'''

    client = self.get_client()
    if client is None:
      show_error("login failed.")
      return 3

    info = client.reload_project()
    show_info("reset %d cron trigger." % info['count'])
    return 0

  def do_export(self, args):
    '''プロジェクトの一覧を CSV 形式で出力する。'''

    usage = "project export <filename>"
    if len(args) != 1:
      show_usage(usage)
      return 1

    try:
      if args[0] != '-':
        fd = open(args[0], 'wb')
      else:
        fd = sys.stdout
    except IOError:
      show_error("cannot open '%s'." % args[0])
      return 2

    try:
      writer = csv.writer(fd)

      row = [ "#" + project_keys[0] ]
      row += [ project_keys[i] for i in xrange(1, len(project_keys)) ]
      row += ["permit"]
      writer.writerow(row)

      for e in self.db.get_all_projects():
        values = [ self.get_csv_value(k, e[k]) for k in project_keys ]
        permit = self.db.get_permit_users(e['title'])
        values.append(" ".join(permit))
        writer.writerow(values)
    finally:
      fd.close()

    return 0

  def do_import(self, args):
    '''プロジェクトの一覧を CSV 形式で取り込む。'''

    usage = "project import <filename>"
    if len(args) != 1:
      show_usage(usage)
      return 1

    try:
      if args[0] != '-':
        fd = open(args[0], 'rb')
      else:
        fd = sys.stdin
    except IOError:
      show_error("cannot open '%s'." % args[0])
      return 2

    new_projects = []
    old_projects = []

    try:
      reader = csv.reader(fd)
      lineno = 0

      for row in reader:
        lineno += 1
        if lineno == 1 and row[0][0] == "#":
          continue

        if len(row) == 0:
          continue

        if len(row) < 14:
          show_error("too few column in line %d." % lineno)
          return 2

        if len(row) > 14:
          show_error("too many column in line %d." % lineno)
          return 2

        (projname, description, license, site_url, download_url,
         src_type, src_path, scm_user, scm_pass, crontab, ignores,
         admin, restricted, permit) = row

        if not self.check_value("title", projname):
          show_error("invalid project-name in line %d." % lineno)
          return 2

        if not self.check_value("description", description):
          show_error("invalid description in line %d." % lineno)
          return 2

        if not self.check_value("license", license):
          show_error("invalid license in line %d." % lineno)
          return 2

        if not self.check_value("src_type", src_type):
          show_error("invalid source type in line %d." % lineno)
          return 2

        if not self.check_value("crontab", crontab):
          show_error("invalid index schedule in line %d." % lineno)
          return 2

        if not self.check_value("restricted", restricted):
          show_error("invalid restricted value in line %d." % lineno)
          return 2

        if admin:
          info = self.db.get_user_by_name(admin)
          if info is None:
            show_error("user '%s' does not exist in line %d." % (admin, lineno))
            return 2

        for u in permit.split():
          info = self.db.get_user_by_name(u)
          if info is None:
            show_error("user '%s' does not exist in line %d." % (u, lineno))
            return 2

        new_projects.append(row)
    finally:
      fd.close()

    for e in self.db.get_all_projects():
      row = [ self.get_csv_value(k, e[k]) for k in project_keys ]
      permit = self.db.get_permit_users(e['title'])
      row.append(" ".join(permit))
      old_projects.append(row)

    old_map = dict((e[0], e) for e in old_projects)
    new_map = dict((e[0], e) for e in new_projects)

    rows_add = [ new_map[e] for e in new_map if e not in old_map ]
    rows_del = [ old_map[e] for e in old_map if e not in new_map ]
    rows_mod = [ new_map[e] for e in new_map \
                 if e in old_map and old_map[e] != new_map[e] ]

    if len(rows_del) > 0 or len(rows_add) > 0 or len(rows_mod) > 0:
      client = self.get_client()
      if client is None:
        show_error("login failed.")
        return 2

    for e in rows_add:
      projname = e[0]
      for i in xrange(1, len(project_keys)):
        e[i] = self.get_db_value(project_keys[i], e[i])
      self.db.add_project(projname, e[1], e[2], e[3], e[4],
                          e[5], e[6], e[7], e[8],
                          e[9], e[10], e[11], e[12])

      self.db.set_permit_users(projname, e[13].split())
      show_info("add project '%s'." % projname)

    for e in rows_mod:
      projname = e[0]
      (old_row, new_row) = (old_map[projname], new_map[projname])
      for i in xrange(1, len(project_keys)):
        if old_row[i] != new_row[i]:
          value = self.get_db_value(project_keys[i], new_row[i])
          self.db.mod_project(projname, project_keys[i], value)
      if old_row[-1] != new_row[-1]:
        self.db.set_permit_users(projname, new_row[-1].split())
      show_info("change project '%s'." % projname)

    for e in rows_del:
      info = self.db.get_project_by_name(e[0])
      client.delete_project(info['name'])
      show_info("delete project '%s'." % info['title'])

    if len(rows_del) > 0 or len(rows_add) > 0 or len(rows_mod) > 0:
      client.reload_project()

    return 0

  def do_files(self, args):
    '''プロジェクトの登録ファイル数を表示する。'''

    files = {}
    langs = set()
    totals = {}

    for e in self.db.get_file_count():
      if e['title'] not in files:
        files[e['title']] = {e['lang'] : e['count']}
      else:
        files[e['title']][e['lang']] = e['count']
      langs.add(e['lang'])

    for e in files:
      totals[e] = sum(files[e].values())

    langs = [ (e, self.get_lang_name(e)) for e in langs ]
    langs = sorted(langs, key=lambda x: x[1])

    line = "%-20s" % "# project"
    line += "".join("%12s" % e[1] for e in langs)
    line += "%14s" % "total"
    print line

    for p in sorted(files):
      line = "%-20s" % p
      for lang, label in langs:
        if lang in files[p]:
          nf = files[p][lang]
        else:
          nf = 0
        line += "%12d" % nf
      line += "%14d" % totals[p]
      print line

    return 0

  def do_lines(self, args):
    '''プロジェクトの登録ファイルの行数を表示する。'''

    lines = {}
    langs = set()
    totals = {}

    for e in self.db.get_file_count():
      if e['title'] not in lines:
        lines[e['title']] = {e['lang']: e['lines']}
      else:
        lines[e['title']][e['lang']] = e['lines']
      langs.add(e['lang'])

    for e in lines:
      lines[e]['total'] = sum(lines[e].values())

    for e in lines:
      totals[e] = sum(lines[e].values())

    langs = [ (e, self.get_lang_name(e)) for e in langs ]
    langs = sorted(langs, key=lambda x: x[1])

    line = "%-20s" % "# project"
    line += "".join("%12s" % e[1] for e in langs)
    line += "%14s" % "total"
    print line

    for p in sorted(lines):
      line = "%-20s" % p
      for lang, label in langs:
        if lang in lines[p]:
          nl = lines[p][lang]
        else:
          nl = 0
        line += "%12d" % nl
      line += "%14d" % totals[p]
      print line

    return 0

  def check_value(self, name, value):
    '''ユーザの入力値をチェックする。'''
    if name == 'title':
      if not projname_pattern.match(value):
        return False
      return True

    if name == 'description':
      try:
        value = unicode(value, 'utf-8')
        return True
      except UnicodeError:
        return False

    if name == 'license':
      if len(value) > 0 and not license_pattern.match(value):
        return False
      return True

    if name == 'src_type':
      return value in src_types

    if name == 'src_path':
      return len(value) > 0

    if name == 'restricted':
      return value in ["0", "1"]

    if name == 'crontab':
      try:
        value = unicode(value, 'ascii').strip()
      except UnicodeError:
        return False

      if len(value) == 0:
        return True

      elem = value.split()
      if len(elem) < 6:
        return False

      return True

  def get_url_name(self, path):
    '''URL 表示名を取得する。'''
    try:
      return urllib.unquote(path)
    except:
      return path

  def get_type_name(self, stype):
    '''プロジェクトの取込手段を取得する。'''
    if stype in src_types:
      return src_types[stype]
    else:
      return "-"

  def get_src_path(self, path):
    '''プロジェクトのパスを取得する。'''
    try:
      return urllib.unquote(path)
    except:
      return path

  def get_restricted_name(self, restricted):
    '''アクセス制限の有無を取得する。'''
    if restricted:
      return u"あり"
    else:
      return u"なし"

  def get_admin_name(self, admin):
    '''プロジェクト管理者のログイン名を取得する。'''
    if admin is not None:
      info = self.db.get_user_by_id(admin)
      if info:
        return info['username']
    return ""

  def get_lang_name(self, lang):
    '''言語名を取得する。'''
    if lang in lang_names:
      return lang_names[lang]
    else:
      return lang

  def get_client(self):
    '''CodeDepot のクライアントを取得する。'''

    base_url = codedepot_url
    username = None
    password = None

    if os.path.exists(admin_cfg_path):

      #
      # [site]
      # url=http://localhost:8080/codedepot/
      # [auth]
      # username=admin
      # password=******
      #

      cfg = ConfigParser.ConfigParser()
      try:
        cfg.read(admin_cfg_path)
        if cfg.has_option("site", "url"):
          base_url = cfg.get("site", "url")
        if cfg.has_section("auth"):
          try:
            username = cfg.get("auth", "username")
            password = cfg.get("auth", "password")
          except:
            pass
      except:
        pass

    if 'CODEDEPOT_URL' in os.environ and os.environ['CODEDEPOT_URL']:
      base_url = os.environ['CODEDEPOT_URL']

    if not username or not password:
      username = raw_input("Enter admin username for codedepot: ")
      password = getpass.getpass("Enter admin password for codedepot: ")

    client = CodeDepotClient(base_url)
    r = client.login(username, password)
    if r is None or r['status'] != 'ok':
      return None

    return client

  def get_csv_value(self, name, value):
    '''csv のカラム値を出力する。'''
    if value is None:
      return ""
    if name in ['site_url', 'download_url']:
      return self.get_url_name(value)
    if name in ['src_path']:
      return self.get_src_path(value)
    if name in ['admin'] and value is not None:
      info = self.db.get_user_by_id(value)
      if info:
        return info['username']
      else:
        return ''
    if name in ['scm_pass']:
      return base64.decodestring(value)
    if isinstance(value, unicode):
      return value.encode("utf-8")
    if isinstance(value, bool):
      return str(int(value))
    return str(value)

  def get_db_value(self, name, value):
    '''db のカラム値を出力する。'''
    if name in ['description']:
      return unicode(value, 'utf-8')
    if name in ['site_url', 'download_url']:
      return urllib.quote(value)
    if name in ['src_path']:
      return urllib.quote(value)
    if name in ['restricted']:
      return bool(int(value))
    if name in ['scm_pass']:
      return base64.encodestring(value).rstrip()
    if name in ['admin']:
      if value:
        info = self.db.get_user_by_name(value)
        if info:
          return info['id']
        else:
          return None
      else:
        return None
    return value

# ----------------------------------------
# Admin command for log
# ----------------------------------------

class LogAdmin:
  def __init__(self, db):
    self.db = db

  def do_command(self, args):
    '''ログ管理のためのコマンドを実行する。'''

    usage = "log {export|purge} [...]"
    usage += "\n  * export  -- export indexer log."
    usage += "\n  * purge   -- purge indexer log."

    if len(args) < 1:
      show_usage(usage)
      return 1

    command, options = args[0], args[1:]

    if command == "export":
      return self.do_export(options)
    if command == "purge":
      return self.do_purge(options)

    show_usage(usage)
    return 1

  def do_export(self, args):
    '''ログの一覧をする。'''

    usage = "user export <filename>"
    if len(args) != 1:
      show_usage(usage)
      return 1

    try:
      if args[0] != '-':
        fd = open(args[0], 'wb')
      else:
        fd = sys.stdout
    except IOError:
      show_error("cannot open '%s'." % args[0])
      return 2

    def get_status(v):
      if v:
        return u'正常'
      else:
        return u'異常'

    def get_period(v):
      r = []
      for i, c in enumerate(reversed(str(v))):
        if i and (not (i % 3)):
            r.insert(0, ',')
        r.insert(0, c)
      return ''.join(r) + u'秒'

    def get_time(v):
      return v.strftime('%F %T')

    def get_utf8_str(value):
      if value is None:
        return ""
      if isinstance(value, unicode):
        return value.encode("utf-8")
      if isinstance(value, bool):
        return str(int(value))
      return str(value)

    try:
      writer = csv.writer(fd)
      row = [ u"#項番", u'開始時刻', u'終了時刻', u'処理時間',
              u'終了状態', u'プロジェクト名', u'メッセージ' ]
      row = [ get_utf8_str(e) for e in row ]
      writer.writerow(row)

      logs = self.db.get_all_logs()
      for i, e in enumerate(logs):
        row = [ i + 1,
          get_time(e['stime']), get_time(e['etime']),
          get_period(e['period']), get_status(e['status']),
          e['title'], e['msg']
        ]
        row = [ get_utf8_str(e) for e in row ]
        writer.writerow(row)
    finally:
      fd.close()

    return 0

  def do_purge(self, args):
    count = self.db.delete_all_logs()
    show_result("delete all logs.")
    return 0

# ----------------------------------------
# show usage
# ----------------------------------------

def show_usage(optstr):
  basename = os.path.basename(sys.argv[0])
  usage = "usage: %s %s" % (basename, optstr)
  if isinstance(usage, unicode):
    usage = usage.encode("utf-8")
  print >> sys.stderr, "%s" % usage

# ----------------------------------------
# show info
# ----------------------------------------

def show_info(msg):
  if isinstance(msg, unicode):
    print >>sys.stderr, msg.encode("utf-8")
  else:
    print >>sys.stderr, msg

# ----------------------------------------
# show error
# ----------------------------------------

def show_error(msg):
  if isinstance(msg, unicode):
    print >>sys.stderr, msg.encode("utf-8")
  else:
    print >>sys.stderr, msg

# ----------------------------------------
# show result
# ----------------------------------------

def show_result(msg):
  if isinstance(msg, unicode):
    print msg.encode("utf-8")
  else:
    print msg

# ----------------------------------------
# main
# ----------------------------------------

if __name__ == "__main__":

  args = sys.argv[1:]
  usage = "{user|project|log} command [...]"

  if len(args) == 0:
    show_usage(usage)
    sys.exit(0)

  prop = os.path.join(properties_folder, "jdbc.properties")
  options = load_jdbc_properties(prop)
  if options is None:
    show_error("Cannot load %s." % prop)
    sys.exit(2)

  db_host = options["hostname"]
  db_port = options["port"]
  db_name = options["path"]
  db_user = options["username"]
  db_pass = options["password"]

  db = CodeDepotDatabase(db_host, db_port, db_name, db_user, db_pass)

  if args[0] == "user":
    admin = AccountAdmin(db)
    retval = admin.do_command(args[1:])
    sys.exit(retval)

  if args[0] == "project":
    admin = ProjectAdmin(db)
    retval = admin.do_command(args[1:])
    sys.exit(retval)

  if args[0] == "log":
    admin = LogAdmin(db)
    retval = admin.do_command(args[1:])
    sys.exit(retval)

  show_usage(usage)
  sys.exit(2)
