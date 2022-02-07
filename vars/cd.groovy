void call() {

    String gitDiff         = "gitDiff"
    String nexusDownload   = 'nexusDownload'
    String run             = "run"
    String test            = "test"
    String gitMergeMaster  = 'gitMergeMaster'
    String gitMergeDevelop = 'gitMergeDevelop'
    String gitTagMaster    = 'gitTagMaster'
 
    String[] stages = [
        gitDiff,
        nexusDownload,
        run,
        test,
        gitMergeMaster,
        gitMergeDevelop,
        gitTagMaster
    ]

    String[] currentStages = []
    currentStages = stages

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    // gitDiff
    if (currentStages.contains(gitDiff)) {
        stage(gitDiff) {
            CURRENT_STAGE = gitDiff
            figlet CURRENT_STAGE
            sh "git config --add remote.origin.fetch +refs/heads/main:refs/remotes/origin/main"
            sh "git fetch --no-tags"
            sh "git diff origin/main origin/${GIT_LOCAL_BRANCH}"
        }
    }

    // nexusDownload
    if (currentStages.contains(nexusDownload)) {
        stage(nexusDownload) {

            String ARTIFACT_GROUP_ID  = sh script: './mvnw help:evaluate -Dexpression=project.groupId -q -DforceStdout', returnStdout: true
            String ARTIFACT_ID  = sh script: './mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout', returnStdout: true
            String ARTIFACT_VERSION  = sh script: './mvnw help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true

            CURRENT_STAGE = nexusDownload
            figlet CURRENT_STAGE
            withCredentials([usernameColonPassword(credentialsId: env.NEXUS_CREDENTIALS_ID, variable: 'NEXUS_CREDENTIALS')]) {
                String repoUrl = "${env.NEXUS_URL}/repository/${env.NEXUS_REPOSITORY_ID}/${ARTIFACT_GROUP_ID}/${ARTIFACT_ID}/${ARTIFACT_VERSION}/${ARTIFACT_ID}-${ARTIFACT_VERSION}.jar";
                println repoUrl
                sh 'curl -u ${NEXUS_CREDENTIALS} "' + repoUrl + '" -o app.jar'
            }
        }
    }

    // run
    if (currentStages.contains(run)) {
        stage(run) {
            CURRENT_STAGE = run
            figlet CURRENT_STAGE
            sh "java -jar app.jar &"
            sleep 20
        }
    }
    
    // test
    if (currentStages.contains(test)) {
        stage(test) {
            CURRENT_STAGE = test
            figlet CURRENT_STAGE
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    // gitMergeMaster
    if (currentStages.contains(gitMergeMaster)) {
        stage(gitMergeMaster) {
            CURRENT_STAGE = gitMergeMaster
            figlet CURRENT_STAGE
            def git = new helpers.Git()
            git.merge("${env.GIT_LOCAL_BRANCH}",'main')
            println "${STAGE_NAME} realizado con exito"
        }
    }
    
    // gitMergeDevelop
    if (currentStages.contains(gitMergeDevelop)) {
        stage(gitMergeDevelop) {
            CURRENT_STAGE = gitMergeDevelop
            figlet CURRENT_STAGE
            def git = new helpers.Git()
            git.merge("${env.GIT_LOCAL_BRANCH}", 'develop')
            println "${STAGE_NAME} realizado con exito"
        }
    }
    
    // gitTagMaster
    if (currentStages.contains(gitTagMaster)) {
        stage(gitTagMaster) {
            CURRENT_STAGE = gitTagMaster
            figlet CURRENT_STAGE
            def git = new helpers.Git()
            git.tag("${env.GIT_LOCAL_BRANCH}")
            println "${STAGE_NAME} realizado con exito"
        }
    }
}

return this