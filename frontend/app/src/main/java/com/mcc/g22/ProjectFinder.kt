package com.mcc.g22

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ProjectFinder {

    companion object {
        private val projectsRef = FirebaseDatabase.getInstance().reference.
                                                        child("projects")

        /**
         * Find a project based on name and keyword.
         * If both name and keyword are null or empty string, onProjectsFound will be called with
         * empty set!
         *
         * @param projectName find a project with a name with contains this string.
         *                      When null or empty project name is ignored
         * @param keyword find a project which has a keyword which contains this string.
         *                      When null or empty keywords are ignored
         * @param onProjectsFound function to be called when some project(s) were found. projects set
         *                          can be empty if there is no project with the given name or
         *                          keyword
         * @param onFailure function to be called when connection to the database cannot be
         *                  established
         */
        fun findProject(projectName: String?, keyword: String?,
                        onProjectsFound: (projectsIds: Set<Project>) -> Unit,
                        onFailure: () -> Unit) {

            val projects = mutableSetOf<Project>()
            projectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (p in dataSnapshot.children) {
                        try {
                            if (isProjectSuitable(p, projectName, keyword)) {
                                projects.add( Project.buildProjectFromDatabase(p) )
                            }
                        } catch (e: Exception){
                            Log.e("MCC", e.toString())
                        }
                    }
                    onProjectsFound(projects)
                }
            })
        }

        @SuppressLint("DefaultLocale")
        private fun isProjectSuitable(projectInDatabase: DataSnapshot,
                                      projectName: String?, keyword: String?): Boolean {
            val user = User.getRegisteredUser() ?: return false

            val checkProjectName = projectName != null && projectName.isNotEmpty()
            val checkKeyword = keyword != null && keyword.isNotEmpty()
            if (!checkProjectName && !checkKeyword) return false

            // We aren't the member of the project
            // TODO should this condition really be? Or we look up in the whole database of projects?
            if (!projectInDatabase.child("members").hasChild(user.username)) {
                return false
            }

            val projectNameFound: Boolean
            var keywordFound = false

            projectNameFound = if (checkProjectName) {
                val projectNameLowerCase = projectName!!.toLowerCase()
                val projectNameInDatabase =
                    (projectInDatabase.child("name").value as String?)!!.toLowerCase()

                projectNameInDatabase.contains(projectNameLowerCase)
            } else true

            if (checkKeyword) {
                val lowerCasesKeyword = keyword!!.toLowerCase()
                if (projectInDatabase.hasChild("keywords")) {
                    for (keywordInDatabase in projectInDatabase.child("keywords").children) {
                        if (keywordInDatabase.key!!.toLowerCase().contains(lowerCasesKeyword)) {
                            keywordFound = true
                            break
                        }
                    }
                }
            } else keywordFound = true

            return projectNameFound && keywordFound
        }
    }
}