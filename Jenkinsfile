pipeline {
    agent any

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
                sh './mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvn test'
            }
        }

        stage('Code Coverage') {
            steps {
                sh './mvn jacoco:report'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/jacoco/*.html', allowEmptyArchive: true
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvn package'
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