#!/bin/bash

# 1. get all i18n.getString("KEY") keys from Java files
grep -rhoP 'i18n\.getString\(\s*"\K[^"]+' . --include=*.java | sort | uniq > all_keys.txt
grep -rhoP 'String\s+jobName\s*=\s*"\K[^"]+' . --include=*.java >> all_keys.txt

# 2. get all properties keys
grep -oP '^[^#!=\s]+' src/main/resources/i18n_zh_CN.properties | sort | uniq > zh_keys.txt
grep -oP '^[^#!=\s]+' src/main/resources/i18n_en_US.properties | sort | uniq > en_keys.txt

# 3. check keys
echo "Missing zh keys:"
echo "-------------------"
grep -Fxv -f zh_keys.txt all_keys.txt

echo ""
echo "Missing en keys:\n"
echo "-------------------"
grep -Fxv -f en_keys.txt all_keys.txt

# 4. clean up temporary files
rm all_keys.txt zh_keys.txt en_keys.txt