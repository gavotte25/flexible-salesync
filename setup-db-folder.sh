minikube cp common.tar /common.tar
minikube ssh "
    cd /
    pwd
    sudo tar -xf common.tar
"