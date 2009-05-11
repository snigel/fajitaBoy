#!/usr/bin/env bash

# echo -n "User: "
# read user
user="arvid"
rsync -zrv dist-web/* -e "ssh -p 2222 " ${user}@sn3gor.olf.sgsnet.se:/var/www/fajitaboy 
