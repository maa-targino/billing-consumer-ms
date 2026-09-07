pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                // InvoicePersistenceAdapterTest usa Testcontainers e precisa de um Docker
                // acessivel; o pod do Jenkins nao tem, entao fica de fora do build da esteira.
                sh "./mvnw clean package -Dtest='!InvoicePersistenceAdapterTest'"
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
        }
        success {
            echo '''
Build e testes OK.

Build da imagem e deploy no minikube ainda sao manuais nessa versao da esteira
(o pod do Jenkins roda dentro do cluster e nao tem kubectl/RBAC/minikube configurados):

  minikube image build -t billing-consumer-ms:latest .
  kubectl apply -f k8s/app.yaml
'''
        }
    }
}
