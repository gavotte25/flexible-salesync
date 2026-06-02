cd yaml-kubectl
kubectl delete -f .
minikube ssh "
    sudo rm /authdb --recursive
    sudo rm /keycloakdb --recursive
    sudo rm /notidb --recursive
    sudo rm /recorddb --recursive
    sudo rm /typedb --recursive
    sudo rm /minio --recursive
" -p baseline
kubectl apply -f .