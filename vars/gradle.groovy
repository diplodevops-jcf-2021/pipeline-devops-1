
void call(String pipelineType) {

    figlet 'Gradle'
    figlet pipelineType

    if (pipelineType.contains('CI-')) {
        runCi(pipelineType)
    } else if (pipelineType == 'CD') {
        cd.call()
    } else {
        throw new Exception('PipelineType Inválido: ' + pipelineType)
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
            sh './gradlew clean compileJava compileTestJava'
        }
    }

    // unitTest
    if (currentStages.contains(stageUnitTest)) {
        stage(stageUnitTest) {
            CURRENT_STAGE = stageUnitTest
            figlet CURRENT_STAGE
            sh './gradlew test'
        }
    }
   
    // jar
    if (currentStages.contains(stageJar)) {
        stage(stageJar) {
            CURRENT_STAGE = stageJar
            figlet CURRENT_STAGE
            sh './gradlew assemble'
        }
    }

    // sonar
    if (currentStages.contains(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            figlet CURRENT_STAGE
            String sonarProjectKey = "ms-iclab-${env.GIT_LOCAL_BRANCH}"
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
                        [classifier: '', extension: '', filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar']
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
