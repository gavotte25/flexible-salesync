minikube cp common.tar /common.tar -p misc-cloud
minikube ssh "
    cd /
    pwd
    sudo tar -xf common.tar
" -p misc-cloud