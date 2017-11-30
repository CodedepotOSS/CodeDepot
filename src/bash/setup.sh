#!/bin/bash -x

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

DATA_ROOT="%{DATA_ROOT}"

if [ ! -d ${DATA_ROOT} ] ; then
    echo $"Create directory \"${DATA_ROOT}\"."
    mkdir ${DATA_ROOT}
fi
for d in index src html tmp dat etc backup
do
    dir=${DATA_ROOT}/${d}
    [ -d ${dir} ] || mkdir ${dir}
done
if [ ! -d ${DATA_ROOT}/index/conf ] ; then
    cp -r data/index/conf ${DATA_ROOT}/index/
fi
if [ ! -d ${DATA_ROOT}/dat/java ] ; then
	mkdir ${DATA_ROOT}/dat/java
fi
if [ ! -d ${DATA_ROOT}/dat/java/globalClass.data ] ; then
	cp data/dat/java/globalClass.data ${DATA_ROOT}/dat/java/globalClass.data
fi

exit 0
