pipeline {
    agent any

    environment {
        JAVA_HOME = tool 'JDK21' // Ensure JDK 21 is configured in Jenkins
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
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

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Code Coverage') {
            steps {
                sh './mvnw jacoco:report'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/jacoco/*.html', allowEmptyArchive: true
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package'
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