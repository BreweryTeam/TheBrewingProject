pipeline {
    stages {
        stage('Build') {
            agent { label 'jdk-21' }

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bukkit:shadowJar'
            }
        }
    }
}
