void call(String pipelineType) {

    figlet 'Maven'
    figlet pipelineType

    if (pipelineType.contains('CI-')) {
        runCi(pipelineType)
    } else if (pipelineType == 'CD') {
        runCd()
    } else {
        throw new Exception('PipelineType Inválido: ' + pipelineType)
    }
}

void runCd() {

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
            sh " git diff origin/main origin/${env:BRANCH_NAME}"
        }
    }

    // nexusDownload
    if (currentStages.contains(nexusDownload)) {
        stage(nexusDownload) {
            CURRENT_STAGE = nexusDownload
            figlet CURRENT_STAGE
            withCredentials([usernameColonPassword(credentialsId: env.NEXUS_CREDENTIALS_ID, variable: 'NEXUS_CREDENTIALS')]) {
                sh 'curl -u ${NEXUS_CREDENTIALS} "${env.NEXUS_URL}/repository/${env.NEXUS_REPOSITORY_ID}/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
            }
        }
    }

    // run
    if (currentStages.contains(run)) {
        stage(run) {
            CURRENT_STAGE = run
            figlet CURRENT_STAGE
            sh 'java -jar DevOpsUsach2020-0.0.1.jar &'
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
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
    
    // gitMergeDevelop
    if (currentStages.contains(gitMergeDevelop)) {
        stage(gitMergeDevelop) {
            CURRENT_STAGE = gitMergeDevelop
            figlet CURRENT_STAGE
            def git = new helpers.Git()
            git.merge("${env.GIT_LOCAL_BRANCH}", 'develop')
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
    
    // gitTagMaster
    if (currentStages.contains(gitTagMaster)) {
        stage(gitTagMaster) {
            CURRENT_STAGE = gitTagMaster
            figlet CURRENT_STAGE
            def git = new helpers.Git()
            git.tag("${env.GIT_LOCAL_BRANCH}")
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
}

void runCi(String pipelineType) {

    String stageCompile  = 'compile'
    String stageUnitTest = 'unitTest'
    String stageJar      = 'jar'
    String stageSonar    = 'sonar'
    String stageNexus    = 'nexusUpload'
    String stageCreateRelease  = 'gitCreateRelease'

    String[] stages = []

    if (pipelineType == 'CI-Feature') {
        stages = [
            stageCompile,
            stageUnitTest,
            stageJar,
            stageSonar,
            stageNexus
        ]
    } else if (pipelineType == 'CI-Develop') {
        stages = [
            stageCompile,
            stageUnitTest,
            stageJar,
            stageSonar,
            stageNexus,
            stageCreateRelease
        ]
    }
    
    String[] currentStages = stages

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    // compile
    if (currentStages.contains(stageCompile)) {
        stage(stageCompile) {
            CURRENT_STAGE = stageCompile
            figlet CURRENT_STAGE
            sh './mvnw clean compile -e'
        }
    }

    // unitTest
    if (currentStages.contains(stageUnitTest)) {
        stage(stageUnitTest) {
            CURRENT_STAGE = stageUnitTest
            figlet CURRENT_STAGE
            sh './mvnw clean test -e'
        }
    }
    
    // jar
    if (currentStages.contains(stageJar)) {
        stage(stageJar) {
            CURRENT_STAGE = stageJar
            figlet CURRENT_STAGE
            sh './mvnw clean package -e'
        }
    }

    // sonar
    if (currentStages.contains(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            String sonarProjectKey = 'ms-iclab-' + ${env.GIT_LOCAL_BRANCH}
            figlet CURRENT_STAGE
            String scannerHome = tool 'sonar-scanner'
            withSonarQubeEnv( env.SONAR_QUBE_ID ) {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${sonarProjectKey} -Dsonar.sources=src -Dsonar.java.binaries=build"
            }
        }
    }

    // nexusUpload
    if (currentStages.contains(stageNexus)) {
        stage(stageNexus) {
            CURRENT_STAGE = stageNexus
            figlet CURRENT_STAGE
            nexusPublisher nexusInstanceId: env.NEXUS_INSTANCE_ID,
            nexusRepositoryId: env.NEXUS_REPOSITORY_ID,
            packages: [
                [
                    $class: 'MavenPackage',
                    mavenAssetList: [
                        [classifier: '', extension: '', filePath: 'build/DevOpsUsach2020-0.0.1.jar']
                    ],
                    mavenCoordinate: [
                        artifactId: 'DevOpsUsach2020',
                        groupId: 'com.devopsusach2020',
                        packaging: 'jar',
                        version: '0.0.1'
                    ]
                ]
            ]
        }
    }
    
    // gitCreateRelease
    if (currentStages.contains(stageCreateRelease)) {
        stage(stageCreateRelease) {
            CURRENT_STAGE = stageCreateRelease
            figlet CURRENT_STAGE
            // TODO: definir stage
            def git = new helpers.Git()
            git.release("release-v1-1-0")
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
}

return this
