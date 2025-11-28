package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter
    private val allRepos = mutableListOf<Repo>()

    private val EDIT_REPO_REQUEST = 100
    private val CREATE_REPO_REQUEST = 101

    private val PREFS_NAME = "deleted_repos_prefs"
    private val DELETED_KEY = "deleted_repo_ids"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchRepositories()

        binding.newRepoFab.setOnClickListener {
            val intent = Intent(this, RepoForm::class.java)
            startActivityForResult(intent, CREATE_REPO_REQUEST)
        }
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            onEdit = { repo ->
                val intent = Intent(this, EditRepoActivity::class.java)
                intent.putExtra("repo", repo)
                startActivityForResult(intent, EDIT_REPO_REQUEST)
            },
            onDelete = { repo ->
                RetrofitClient.getApiService()?.deleteRepo(repo.owner.login, repo.name)
                    ?.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                saveDeletedRepoId(repo.id)
                                allRepos.remove(repo)
                                reposAdapter.updateRepositories(allRepos)
                                showMessage("Repositorio eliminado de GitHub")
                            } else {
                                showMessage("Error al eliminar: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            showMessage("Error de red al eliminar")
                        }
                    })
            }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        val deletedIds = getDeletedRepoIds()

        RetrofitClient.getApiService()?.getRepos()?.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()?.filterNot { deletedIds.contains(it.id.toString()) } ?: emptyList()
                    allRepos.clear()
                    allRepos.addAll(repos)
                    reposAdapter.updateRepositories(allRepos)
                } else {
                    showMessage("Error al cargar: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                showMessage("Error de red al cargar repositorios")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            EDIT_REPO_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val updatedRepo = data?.getSerializableExtra("updatedRepo") as? Repo ?: return
                    val index = allRepos.indexOfFirst { it.id == updatedRepo.id }
                    if (index != -1) {
                        allRepos[index] = updatedRepo
                        reposAdapter.updateRepositories(allRepos)
                        showMessage("Repositorio actualizado")
                    }
                }
            }

            CREATE_REPO_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val newRepo = data?.getSerializableExtra("newRepo") as? Repo ?: return
                    allRepos.add(0, newRepo)
                    reposAdapter.updateRepositories(allRepos)
                    showMessage("Repositorio creado")
                }
            }
        }
    }

    private fun saveDeletedRepoId(id: Long) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val ids = prefs.getStringSet(DELETED_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        ids.add(id.toString())
        prefs.edit().putStringSet(DELETED_KEY, ids).apply()
    }

    private fun getDeletedRepoIds(): Set<String> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getStringSet(DELETED_KEY, mutableSetOf()) ?: emptySet()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}