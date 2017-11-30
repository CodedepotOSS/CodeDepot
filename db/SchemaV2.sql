--
-- Copyright (c) 2009 SRA (Software Research Associates, Inc.)
--
-- This file is part of CodeDepot.
-- CodeDepot is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License version 3.0
-- as published by the Free Software Foundation and appearing in
-- the file GPL.txt included in the packaging of this file.
--
-- CodeDepot is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
--

DROP VIEW totalsource;
--
-- Create table for User Account
--

DROP TABLE member;
CREATE TABLE member
(	id		SERIAL PRIMARY KEY,
        username	VARCHAR(40),		-- Login Name
        email		VARCHAR(1024),		-- Mail Address
        password	VARCHAR(80),		-- Hashed Password
        pwd_mtime	TIMESTAMP,
	role		INTEGER DEFAULT 0,
	active		BOOLEAN DEFAULT TRUE,
	def_lang	VARCHAR(40) DEFAULT 'java',
	note		VARCHAR(2048),
	del_flag	BOOLEAN DEFAULT FALSE,
	cuserid		INTEGER NOT NULL,
	ctime		TIMESTAMP NOT NULL,
	muserid		INTEGER NOT NULL,
	mtime		TIMESTAMP NOT NULL
);

--
-- Insert Initial Admin User:
--   Username: "admin"
--   Password: "admin"
--

INSERT INTO member (username, email, role, note, password,
                    pwd_mtime, cuserid, ctime, muserid, mtime)
        VALUES ('admin', '', 2, '初期システム管理者',
                '0DPiKuNIrrVmD8IUCuw1hQxNqZc=', now(),
                1, now(), 1, now());

--
-- Create table for Project Information
--

DROP TABLE project;
CREATE TABLE project
(
	id		SERIAL PRIMARY KEY,
	name		VARCHAR(40) UNIQUE,
	title		VARCHAR(80),
	description	VARCHAR(2048),
	license		VARCHAR(80),
	site_url	VARCHAR(1024),
	download_url	VARCHAR(1024),
	restricted	BOOLEAN DEFAULT FALSE,
	src_type	VARCHAR(20),
	src_path	VARCHAR(1024),
	scm_user	VARCHAR(40),
	scm_pass	VARCHAR(80),
	crontab		VARCHAR(80),
	admin		INTEGER,
	del_flag	BOOLEAN DEFAULT FALSE,
	ignores		VARCHAR(1024),
	cuserid		INTEGER NOT NULL,
	ctime		TIMESTAMP NOT NULL,
	muserid		INTEGER NOT NULL,
	mtime		TIMESTAMP NOT NULL,
	indexed_at	TIMESTAMP
);

--
-- Create table for Project Permission
--

DROP TABLE permit;
CREATE TABLE permit
(
	mid		INTEGER,
	project		VARCHAR(40),
	role		INTEGER DEFAULT 0
);

--
-- Create table for Indexed Source
--

DROP TABLE source;
CREATE TABLE source
(
	project		VARCHAR(40),
	path		VARCHAR(4096),
	ctime		TIMESTAMP NOT NULL,
	mtime		TIMESTAMP NOT NULL,
	lang		VARCHAR(20),
	size		BIGINT,
	lines		BIGINT,
	digest		VARCHAR(80),
	PRIMARY KEY	(project, path)
);

--
-- Create table for file used in note table.
--

DROP TABLE file;
CREATE TABLE file
(
	id		BIGSERIAL PRIMARY KEY,
	project		VARCHAR(40),
	path		VARCHAR(4096),
	UNIQUE		(project, path)
);

--
-- Create table for note.
--

DROP TABLE note;
CREATE TABLE note
(
	id		BIGSERIAL PRIMARY KEY,
	fid		BIGINT,
	linefrom	INTEGER,
	lineto		INTEGER,
	contents	TEXT,
	public		BOOLEAN DEFAULT TRUE,
	del_flag	BOOLEAN DEFAULT FALSE,
	cuserid		INTEGER NOT NULL,
	ctime		TIMESTAMP NOT NULL,
	muserid		INTEGER NOT NULL,
	mtime		TIMESTAMP NOT NULL
);

--
-- Create view for totalsource
--

CREATE VIEW totalsource AS
(
	SELECT	source.project as project,
			source.lang as lang,
			COUNT(1) as num,
			SUM(lines) as lines
	FROM	source
	GROUP BY project, lang
	ORDER BY lang
);

-- Table: batchlog

-- DROP TABLE batchlog;
DROP TABLE batchlog;
CREATE TABLE batchlog
(
	stime		TIMESTAMP,
	etime		TIMESTAMP,
	period		INTEGER,
	project		VARCHAR(40),
	msg		VARCHAR(2048),
	status		BOOLEAN DEFAULT TRUE
);


-- Table: version
-- DROP TABLE version;

DROP TABLE version;
CREATE TABLE version
(
	kind		VARCHAR(20) UNIQUE,
	vernum		INTEGER,
	mtime		TIMESTAMP
);

INSERT INTO version (kind, vernum, mtime)
        VALUES ('database', 3, '2011-08-04 00:00:00');

INSERT INTO version (kind, vernum, mtime)
        VALUES ('indexer', 11, '2017-06-01 00:00:00');
