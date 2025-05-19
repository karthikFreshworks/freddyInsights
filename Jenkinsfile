pipeline {
    agent {
        kubernetes {
              inheritFrom 'eks-freshiq-java21'
              defaultContainer 'eks-freshiq-java21'
              label 'eks-freshiq-java21'
          }
      }

    triggers {
        githubPush() // Trigger builds on push events
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean install -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploy stage - Add deployment steps here'
            }
        }
    }

    post {
        always {
            cleanWs() // Clean workspace after build
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}