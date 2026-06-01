minikube cp common.tar /common.tar -p baseline
minikube ssh "
    cd /
    pwd
    sudo tar -xf common.tar
" -p baseline