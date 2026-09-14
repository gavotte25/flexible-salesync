# flexible-salesync
This branch (`misc-cloud`) adds the MiSC-Cloud core infrastructure — `tenant-manager/` and `customization-gateway/` — on top of the base deployment. The steps below deploy the full stack including these two services; see "Configuring customizations for tenants" for how to turn a deployed customization on for a tenant.
### Prerequisite
- Install Java JDK for Keytool utility
- Register dynamic domain via DNS provider like [No-Ip](https://www.noip.com/)
- Install [minikube](https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download) cluster.
### Fetching project
```
git clone -b misc-cloud https://github.com/gavotte25/flexible-salesync.git flexible-salesync-misc-cloud
cd flexible-salesync-misc-cloud
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
minikube start --cpus 4 --memory 24000 --ports=80 -p misc-cloud
minikube addons enable ingress -p misc-cloud
```
### Prepare docker images
`authentication`, `notification`, and `type` are unchanged from `baseline` on this branch — reuse their existing `:latest` images (no need to rebuild) if you already have them loaded into this cluster. `frontend`, `record`, `tenant-manager`, and `customization-gateway` are new/changed on `misc-cloud`, tagged accordingly so they don't collide with any `:latest` build from another branch — the k8s manifests in `yaml-kubectl/` reference these same tags
```
docker build -t authentication authentication/
docker build -t frontend:misc-cloud frontend/
docker build -t notification notification/
docker build -t record:misc-cloud record/
docker build -t type type/
docker build -t tenant-manager:misc-cloud tenant-manager/
docker build -t customization-gateway:misc-cloud customization-gateway/
```
Import images to minikube cluster
```
minikube image load frontend:misc-cloud -p misc-cloud
minikube image load authentication:latest -p misc-cloud
minikube image load notification:latest -p misc-cloud
minikube image load record:misc-cloud -p misc-cloud
minikube image load type:latest -p misc-cloud
minikube image load tenant-manager:misc-cloud -p misc-cloud
minikube image load customization-gateway:misc-cloud -p misc-cloud
```
### Import database preconfig to minikube
```
sh setup-db-folder.sh
```
### Initialize all services
```
kubectl apply -f yaml-kubectl/
minikube tunnel --bind-address="0.0.0.0" -p misc-cloud
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
### Configuring customizations for tenants
`tenant-manager` is internal-only (no Ingress) - reach it by port-forwarding first:
```
kubectl port-forward svc/tenant-manager-service 5100:5100
```
A customization microservice is deployed **once** and self-registers what it offers (tenant-agnostic) at startup - see `tenant-a-record-export/entrypoint.sh` for an example. Onboarding a tenant onto an already-deployed offering is then just an API call, not a new deployment:
```
# List what's available to activate
curl http://localhost:5100/offerings

# Activate an offering for a tenant
curl -X POST http://localhost:5100/activations \
  -H "Content-Type: application/json" \
  -d '{"realmName": "<tenant-realm>", "offeringKey": "<offering-key>"}'

# Deactivate it
curl -X DELETE http://localhost:5100/activations/<tenant-realm>/<offering-key>
```
A tenant's currently active customizations: `curl http://localhost:5100/registrations/<tenant-realm>`.
