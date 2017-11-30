#!/bin/bash

####
# Copyright (c) 2009 SRA (Software Research Associates, Inc.)
#
# This file is part of CodeDepot.
# CodeDepot is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 3.0
# as published by the Free Software Foundation and appearing in
# the file GPL.txt included in the packaging of this file.
#
# CodeDepot is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
####

PGPORT="%{PGPORT}"
DBNAME="%{DBNAME}"
DBUSER="%{DBUSER}"
DBPASS="%{DBPASS}"

DB_SCRIPT="db/SchemaV2.sql"
DB_ADMIN=$(id -un)

QUIET=0
LOG=/tmp/initdb.log

if [ "${DB_ADMIN}" != "${DBUSER}" ] ; then
	[ ${QUIET} -ne 0 ] || echo -n $"Create database user \"${DBUSER}\": "
	createuser -U "${DB_ADMIN}" -p ${PGPORT} -S -d -r "${DBUSER}" >>${LOG} 2>&1
	echo

	[ ${QUIET} -ne 0 ] || echo -n $"Setting Password for \"${DBUSER}\": "
	psql -q -U ${DB_ADMIN} -p ${PGPORT} -o /dev/null \
		-c "ALTER USER \"${DBUSER}\" ENCRYPTED PASSWORD '${DBPASS}';" template1 >>${LOG} 2>&1
	echo

	[ ${QUIET} -ne 0 ] || echo -n $"Create Database \"${DBNAME}\": "
		createdb -U ${DB_ADMIN} -p ${PGPORT} -O ${DBUSER} ${DBNAME} >>${LOG} 2>&1
	echo
fi

if [ -n "${DB_SCRIPT}" ] ; then
	[ ${QUIET} -ne 0 ] || echo -n $"Initialize Database \"${DBNAME}\": "
	psql -W -q -U ${DBUSER} -p ${PGPORT} -d ${DBNAME} -o /dev/null -f ${DB_SCRIPT} >>${LOG} 2>&1
	echo
fi

exit 0
