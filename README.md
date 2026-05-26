# flexible-salesync customized for tenant A
Customized service:
- Frontend: Change the color scheme

## Prerequisite
Base system on main branch of flexible-salesync has already been deployed.

## Buid image
```
git clone -b main-tenant-a https://github.com/gavotte25/flexible-salesync.git flexible-salesync-tenant-a
cd flexible-salesync-tenant-a
git submodule update --init --recursive --force --remote
```
## Build image and deploy
```
docker build -t frontend-tenant-a frontend/
minikube image load frontend-tenant-a:latest
kubectl apply -f yaml-kubectl/.
```
