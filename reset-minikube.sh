cd yaml-kubectl
kubectl delete -f 03-storage.yaml
kubectl delete -f 04-frontend.yaml
kubectl delete -f 05-mq.yaml
kubectl delete -f 06-keycloak.yaml
kubectl delete -f 07-auth.yaml
kubectl delete -f 08-noti.yaml
kubectl delete -f 09-type.yaml
kubectl delete -f 10-record.yaml
minikube ssh "
    sudo rm /authdb --recursive
    sudo rm /keycloakdb --recursive
    sudo rm /notidb --recursive
    sudo rm /recorddb --recursive
    sudo rm /typedb --recursive
    sudo rm /minio --recursive
"
kubectl apply -f .