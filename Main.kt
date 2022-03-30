package tasklist

import java.lang.System
import java.util.Scanner
import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.EOFException
import java.io.File


class Task(var date: String, var time: String, var priority: String, var deadline: String, var task: String)

fun main() {
    val fileName = "tasklist.json"
    val jsonFile = File(fileName)

    if (!File(fileName).exists()) {
        jsonFile.writeText("")
    }

    val scanner = Scanner(System.`in`)
    val taskList = extractTasks(jsonFile)

    while (true) {
        println("Input an action (add, print, edit, delete, end): ")

        val command = scanner.nextLine().strip()

        when (command) {
            "add" -> {
                val priority = setPriority()

                val date = setDate()

                var time = setTime(date)

                println("Input a new task (enter a blank line to end): ")
                var newTask = inputTask()

                if (newTask == "") {
                    println("The task is blank")
                } else {
                    newTask = newTask.substring(0, newTask.length - 1)
                    val timeParts = time.split(":")
                    val dateParts = date.split("-")
                    val taskDate = LocalDate(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
                    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+2")).date
                    val numberOfDays = currentDate.daysUntil(taskDate)

                    if (timeParts[0].length == 1 && timeParts[1].length == 1) {
                        time = "0${timeParts[0]}:0${timeParts[1]}"
                    } else if (timeParts[0].length == 1) {
                        time = "0${timeParts[0]}:${timeParts[1]}"
                    } else if (timeParts[1].length == 1){
                        time = "${timeParts[0]}:0${timeParts[1]}"
                    }

                    if (numberOfDays == 0) {
                        val taskToAdd = Task(date, time, priority.uppercase(), "T", newTask)
                        taskList.add(taskToAdd)

                    } else if (numberOfDays > 0) {
                        val taskToAdd = Task(date, time, priority.uppercase(), "I", newTask)
                        taskList.add(taskToAdd)

                    } else if (numberOfDays < 0) {
                        val taskToAdd = Task(date, time, priority.uppercase(), "O", newTask)
                        taskList.add(taskToAdd)
                    }
                }
            }

            "print" -> {
                printTasks(taskList)
            }

            "edit" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                } else {
                    printTasks(taskList)

                    println("Input the task number (1-${taskList.size}):")
                    var taskNum = scanner.nextLine()

                    var taskEditNum: Int

                    while (true) {
                        try {
                            val num = taskNum.toInt()

                            if (num in 1..taskList.size) {
                                taskEditNum = num
                                break
                            }

                            else {
                                println("Invalid task number")
                                println("Input the task number (1-${taskList.size}):")
                                taskNum = scanner.nextLine()
                            }
                        }

                        catch (e: java.lang.NumberFormatException) {
                            println("Invalid task number")
                            println("Input the task number (1-${taskList.size}):")
                            taskNum = scanner.nextLine()
                        }
                    }

                    val taskToEdit = taskList[taskEditNum - 1]

                    println("Input a field to edit (priority, date, time, task): ")
                    var field = scanner.nextLine().lowercase()

                    while (field !in mutableListOf("priority", "date", "time", "task")) {
                        println("Invalid field")
                        println("Input a field to edit (priority, date, time, task): ")
                        field = scanner.nextLine().lowercase()
                    }

                    when (field) {
                        "priority" -> {
                            val priority = setPriority()

                            taskToEdit?.priority = priority
                        }

                        "date" -> {
                            val date = setDate()

                            taskToEdit?.date = date
                            // taskList[taskEditNum - 1] = taskToEdit.replace(oldValues[0], date)
                        }

                        "time" -> {
                            val time = setTime(taskToEdit?.date)

                            taskToEdit?.time = time
                        }

                        "task" -> {
                            println("Input a new task (enter a blank line to end): ")
                            var newTask = inputTask()

                            if (newTask == "") {
                                println("The task is blank")
                            } else {
                                newTask = newTask.substring(0, newTask.length - 1)
                                taskToEdit?.task = newTask
                                /* val idx = taskToEdit.indexOf("\n")
                                taskList[taskEditNum - 1] = taskToEdit.replace(taskToEdit.substring(idx + 1), newTask) */
                            }
                        }
                    }

                    println("The task is changed")
                }
            }

            "delete" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                } else {
                    printTasks(taskList)

                    println("Input the task number (1-${taskList.size}):")
                    var taskNum = scanner.nextLine()

                    while (true) {
                        try {
                            val num = taskNum.toInt()

                            if (num in 1..taskList.size) {
                                taskList.removeAt(num - 1)
                                println("The task is deleted")
                                break
                            }

                            else {
                                println("Invalid task number")
                                println("Input the task number (1-${taskList.size}):")
                                taskNum = scanner.nextLine()
                            }
                        }

                        catch (e: java.lang.NumberFormatException) {
                            println("Invalid task number")
                            println("Input the task number (1-${taskList.size}):")
                            taskNum = scanner.nextLine()
                        }
                    }
                }
            }

            "end" -> {
                println("Tasklist exiting!")
                addToFile(taskList, jsonFile)
                break
            }

            else -> println("The input action is invalid")
        }
    }
}

fun inputTask(): String {
    val scanner = Scanner(System.`in`)
    var newTask = ""

    while (true) {

        val task = scanner.nextLine().strip()

        if (task == "") {
            break
        } else {
            newTask += task + "\n"
        }
    }
    return newTask
}

fun setPriority(): String {
    val scanner = Scanner(System.`in`)

    println("Input the task priority (C, H, N, L):")
    var priority = scanner.nextLine()

    while (!mutableListOf("C", "H", "N", "L").contains(priority.uppercase())) {
        println("Input the task priority (C, H, N, L):")
        priority = scanner.nextLine()
    }

    return priority
}

fun setDate(): String {
    val scanner = Scanner(System.`in`)

    var dateCondition = false
    var date = ""

    while (!dateCondition) {
        println("Input the date (yyyy-mm-dd):")
        date = scanner.nextLine().strip()
        val dateParts = date.split("-")

        try {
            val localDate = LocalDate(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
            dateCondition = true
            date = localDate.toString()
        } catch (e:java.lang.IllegalArgumentException) {
            println("The input date is invalid")
        }
    }

    return date
}

fun setTime(date: String?): String {
    val scanner = Scanner(System.`in`)

    var timeCondition = false
    var time = ""

    while (!timeCondition) {
        println("Input the time (hh:mm):")
        time = scanner.nextLine().strip()
        val dateParts = date?.split("-")!!

        val timeParts = time.split(":")

        try {
            val localTime = LocalDateTime(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt(), timeParts[0].toInt(), timeParts[1].toInt())
            timeCondition = true
            time = "${localTime.hour}:${localTime.minute}"
        } catch (e: java.lang.IllegalArgumentException) {
            println("The input time is invalid")
        }
    }

    return time
}

fun printTasks(taskList: MutableList<Task?>) {
    val header = "+----+------------+-------+---+---+--------------------------------------------+\n" +
                 "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                 "+----+------------+-------+---+---+--------------------------------------------+"

    val taskEndLine = "+----+------------+-------+---+---+--------------------------------------------+"

    if (taskList.isEmpty()) {
        println("No tasks have been input")
    } else {
        println(header)

        for ((idx, task) in taskList.withIndex()) {
            val taskParts = task?.task?.split("\n")

            val date = task?.date
            val time = task?.time
            val priority = task?.priority
            val deadline = task?.deadline
            var pColor = ""
            var dColor = ""

            when(priority) {
                "C" -> {
                    pColor = "\u001B[101m \u001B[0m"
                }

                "H" -> {
                    pColor = "\u001B[103m \u001B[0m"
                }

                "N" -> {
                    pColor = "\u001B[102m \u001B[0m"
                }

                "L" -> {
                    pColor = "\u001B[104m \u001B[0m"
                }
            }

            when(deadline) {
                "I" -> {
                    dColor = "\u001B[102m \u001B[0m"
                }

                "T" -> {
                    dColor = "\u001B[103m \u001B[0m"
                }

                "O" -> {
                    dColor = "\u001B[101m \u001B[0m"
                }
            }

            var taskHeader = "| ${idx + 1}  | $date | $time | $pColor | $dColor "

            var task = ""

            for (i in 0..taskParts?.lastIndex!!) {

                if (i == 0) {
                    if (taskParts[i].length > 44) {
                        if (i == taskParts.lastIndex) {
                            task = "|${taskParts[i].substring(0, 44)}|\n" +
                                   "|    |            |       |   |   |${taskParts[i].substring(44)}${" ".repeat(44 - taskParts[i].substring(44).length)}|"
                        } else {
                            task = "|${taskParts[i].substring(0, 44)}|\n" +
                                   "|    |            |       |   |   |${taskParts[i].substring(44)}${" ".repeat(44 - taskParts[i].substring(44).length)}|\n"
                        }

                    } else {
                        if (i == taskParts.lastIndex) {
                            task = "|${taskParts[i]}${" ".repeat(44-taskParts[i].length)}|"
                        } else {
                            task = "|${taskParts[i]}${" ".repeat(44-taskParts[i].length)}|\n"
                        }
                    }

                    taskHeader += task

                } else {
                    if (taskParts[i].length > 44) {
                        if (i == taskParts.lastIndex) {
                            task = "|    |            |       |   |   |${taskParts[i].substring(0, 44)}|\n" +
                                   "|    |            |       |   |   |${taskParts[i].substring(44)}${" ".repeat(44 - taskParts[i].substring(44).length)}|"
                        } else {
                            task = "|    |            |       |   |   |${taskParts[i].substring(0, 44)}|\n" +
                                   "|    |            |       |   |   |${taskParts[i].substring(44)}${" ".repeat(44 - taskParts[i].substring(44).length)}|\n"
                        }

                    } else {
                        if (i == taskParts.lastIndex) {
                            task = "|    |            |       |   |   |${taskParts[i]}${" ".repeat(44 - taskParts[i].length)}|"
                        } else {
                            task = "|    |            |       |   |   |${taskParts[i]}${" ".repeat(44 - taskParts[i].length)}|\n"
                        }
                    }

                    taskHeader += task
                }
            }
            println(taskHeader)
            println(taskEndLine)
        }
    }
}

fun extractTasks(jsonFile: File): MutableList<Task?> {
    val taskList = mutableListOf<Task?>()

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val taskAdapter = moshi.adapter(Task::class.java)

    val lines = jsonFile.readLines().toMutableList()

    if (lines.size > 1) {
        for ((idx, jsonLine) in lines.withIndex()) {
            if (jsonLine[jsonLine.lastIndex] == ',') {
                if (idx == 0) {
                    val taskToAdd = taskAdapter.fromJson(jsonLine.replace("[", "").replace("]", "").substring(0, jsonLine.lastIndex - 1))
                    taskList.add(taskToAdd)
                } else {
                    val taskToAdd = taskAdapter.fromJson(jsonLine.replace("[", "").replace("]", "").substring(0, jsonLine.lastIndex))
                    taskList.add(taskToAdd)
                }
            } else {
                val taskToAdd = taskAdapter.fromJson(jsonLine.replace("[", "").replace("]", ""))
                taskList.add(taskToAdd)
            }
        }
    }

    return taskList
}

fun addToFile(taskList: MutableList<Task?>, file: File) {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val taskAdapter = moshi.adapter(Task::class.java)

    val lines = file.readLines().toString()

    if (file.readLines().isEmpty()) {
        file.writeText("[")
        for ((idx, task) in taskList.withIndex()) {
            if (idx == 0) {
                file.appendText(taskAdapter.toJson(task))
            } else {
                file.appendText(",\n" + taskAdapter.toJson(task))
            }
        }
        file.appendText("]")

    } else {
        file.writeText("[" + lines.substring(2, lines.lastIndex - 1))
        for ((idx, task) in taskList.withIndex()) {
            if (idx == 0) {
                file.appendText(",\n" + taskAdapter.toJson(task))
            }
        }
        file.appendText("]")
    }
}
