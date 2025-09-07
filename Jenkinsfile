pipeline {
    agent none
    
    stages {
        stage('Build') {
            agent { label 'linux-jdk21' }

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bukkit:shadowJar migration:shadowJar'
                script {
                    def sanitizedBranch = env.BRANCH_NAME.replaceAll(/[^a-zA-Z0-9._]/, '_')
                    def shortHash = env.GIT_COMMIT.substring(0, 6)
                    def jars = findFiles(glob: 'bukkit/build/libs/TheBrewingProject*.jar') + findFiles(glob: 'migration/build/libs/TheBrewingProjectMigration*.jar')
                    
                    jars.each { jar ->
                        def newPath = jar.path.replaceFirst(/\.jar$/, "-${sanitizedBranch}-${shortHash}.jar")
                        sh "mv '${jar.path}' '${newPath}'"
                    }
                }
            }

            post {        
                always {
                    archiveArtifacts artifacts: 'bukkit/build/libs/TheBrewingProject*.jar, migration/build/libs/TheBrewingProjectMigration*.jar', fingerprint: true
                }
            }
        }
    }
}
