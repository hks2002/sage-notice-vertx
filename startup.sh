#!/usr/bin/bash

./shutdown.sh
java -jar sage-notice-vertx.jar --conf=config-prod.json --options=vertx-options.json
