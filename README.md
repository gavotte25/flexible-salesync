# flexible-salesync
### Prerequisite
- Install Java JDK for Keytool utility
- Register dynamic domain via DNS provider like [No-Ip](https://www.noip.com/)
- Install [minikube](https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download) cluster.
### Fetching project
```
git clone -b baseline https://github.com/gavotte25/flexible-salesync.git flexible-salesync-baseline
cd flexible-salesync-baseline
git submodule update --init --recursive --force --remote
### Replace with your dynamic domain
Find all "salesync.ddns.net" strings in folder yaml-kubectl and init-storage.sh fileand replace with your own dynamic domain
```
### Set up envinronment variables
Replace all values that have TODO comments with your own credentials in 00-secrets.yaml
#### (Optional) To generate keystore, keystore password & publickey, run below commands
```
keytool -genkey -keyalg RSA -v -keystore keystore.jks -alias mykey
keytool -exportcert -alias mykey -keystore keystore.jks -rfc -file publickey.pem
cat keystore.jks| base64
cat publickey.pem| base64
```
### Set up minikube
Start new minikube cluster
```
minikube start --cpus 4 --memory 24000 --ports=80 -p baseline
minikube addons enable ingress -p baseline
```
### Prepare docker images
Build docker images (openjdk:24-jdk-slim is deprecated, use openjdk:27-ea-jdk-slim instead)
```
docker build -t authentication authentication/
docker build -t frontend frontend/
docker build -t notification notification/
docker build -t record record/
docker build -t type type/
```
Import images to minikube cluster
```
minikube image load frontend:latest -p baseline
minikube image load authentication:latest -p baseline
minikube image load notification:latest -p baseline
minikube image load record:latest -p baseline
minikube image load type:latest -p baseline
```
### Import database preconfig to minikube
```
sh setup-db-folder.sh
```
### Initialize all services
```
kubectl apply -f yaml-kubectl/
minikube tunnel --bind-address="0.0.0.0" -p baseline
```
### Set up storage
In case you have different secret settings, update the script below before running, make sure minio credentials match the 00-secrets.yaml file
#### On Windows
```
sh init-storage-win.sh
```
#### On other systems (with [mc command line](<https://docs.min.io/community/minio-object-store/reference/minio-mc.html#install-mc)>) is installed)
```
sh init-storage.sh
```