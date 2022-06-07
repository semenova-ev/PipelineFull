import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.1"

project {

    subProject(TodoBackend)
}


object TodoBackend : Project({
    name = "TodoBackend"

    vcsRoot(TodoBackendVcs)

    buildType(TestReport)
    buildType(TodoApp)
    buildType(Test1)
    buildType(TodoImage)
})

object Test1 : BuildType({
    name = "Test1"

    vcs {
        root(TodoBackendVcs, "+:test1=>.")

        cleanCheckout = true
    }

    steps {
        gradle {
            tasks = "test"
        }
    }

})

object TestReport : BuildType({
    name = "TestReport"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(TodoBackendVcs)

        showDependenciesChanges = true
    }

})

object TodoApp : BuildType({
    name = "TodoApp"

    artifactRules = "build/libs/todo.jar"

    vcs {
        root(TodoBackendVcs, "-:docker")

        cleanCheckout = true
    }

    steps {
        gradle {
            tasks = "clean build"
            buildFile = ""
            gradleWrapperPath = ""
        }
    }
})

object TodoImage : BuildType({
    name = "TodoImage"

    vcs {
        root(TodoBackendVcs, "+:docker=>docker")

        cleanCheckout = true
    }

    steps {
        dockerCommand {
            commandType = build {
                source = path {
                    path = "./docker/Dockerfile"
                }
                contextDir = "."
                namesAndTags = "mkjetbrains/todo-backend:%build.number%"
                commandArgs = "--pull"
            }
        }
    }

 
})

object TodoBackendVcs : GitVcsRoot({
    name = "TodoBackendVcs"
    url = "https://github.com/semenova-ev/Build-Chain-Project"
})
