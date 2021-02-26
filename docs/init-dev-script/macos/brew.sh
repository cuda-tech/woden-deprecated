

echo "修复 macos 下的 mardiaDB4j 问题，ref -> https://github.com/vorburger/MariaDB4j/issues/48"
sudo ln -s /usr/lib/libssl.dylib /usr/local/opt/openssl/lib/libssl.1.0.0.dylib
sudo ln -s /usr/lib/libcrypto.dylib /usr/local/opt/openssl/lib/libcrypto.1.0.0.dylib