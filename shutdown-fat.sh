#!/usr/bin/bash

ps aux | grep sage-notice-vertx-fat.jar | head -n 1 | awk '{print $2}' | xargs kill -9
