#!/usr/bin/bash

./shutdown-fat.sh
java -jar sage-notice-vertx-fat.jar --conf=config-prod.json --options=vertx-options.json

