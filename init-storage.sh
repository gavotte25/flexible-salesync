# Change below fields accordingly based on your server actual settings
# ./mc alias set myminio <MINIO_API_URL> <ACCESS_KEY> <SECRET_KEY>
./mc alias set myminio http://storage.salesync.ddns.net miniouser Aa@12345
./mc mb myminio/system
./mc mb myminio/avatars
./mc mb myminio/companies
./mc anonymous set download myminio/system
./mc anonymous set download myminio/avatars
./mc anonymous set download myminio/companies
./mc cp --recursive assets/system/ myminio/system/
./mc cp --recursive assets/avatars/ myminio/avatars/
./mc cp --recursive assets/companies/ myminio/companies/