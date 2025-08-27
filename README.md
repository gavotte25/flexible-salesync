# flexible-salesync
### Prerequisite
- Install Java JDK for Keytool utility
- Register dynamic domain via DNS provider like [No-Ip](https://www.noip.com/)
- Install [minikube](https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download) cluster.
- Install Istioctl via [Istio installation](https://istio.io/latest/docs/setup/additional-setup/download-istio-release/) or [Istioctl separate binary](https://github.com/istio/istio/releases) (Windows user can refer this [instruction](https://gist.github.com/VidyasagarMSC/2dcb760297f97220fb5e24621c606d76))
### Fetching project
```
git clone https://github.com/gavotte25/flexible-salesync.git
cd flexible-salesync
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
minikube start --cpus 4 --memory 24000 --ports=80
minikube addons enable istio-provisioner
minikube addons enable istio
minikube addons enable ingress
```
Add a namespace label to instruct Istio to automatically inject Envoy sidecar proxies
```
kubectl label namespace default istio-injection=enabled
kubectl label namespace ingress-nginx istio-injection=enabled

```
### Set up istio
Install Istio with an extension provider referring to the Jaeger collector service
```
istioctl install -f yaml-istioctl/tracing.yaml --skip-confirmation
```
### Prepare docker images
Build docker images
```
docker build -t authentication authentication/
docker build -t frontend frontend/
docker build -t notification notification/
docker build -t record record/
docker build -t type type/
```
Import images to minikube cluster
```
minikube image load frontend:latest
minikube image load authentication:latest
minikube image load notification:latest
minikube image load record:latest
minikube image load type:latest
```
### Import database preconfig to minikube
```
sh setup-db-folder.sh
```
### Initialize all services
```
kubectl apply -f yaml-kubectl/
minikube tunnel
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
### Monitoring
Run below commands to use monitoring tools
```
istioctl dashboard jaeger
istioctl dashboard kiali
istioctl dashboard grafana
```
## Usefull commands
```
# Shell in pod
kubectl exec --stdin --tty <POD_NAME> -- /bin/bash
# Log in pod
kubectl logs -f <POD_NAME>
```