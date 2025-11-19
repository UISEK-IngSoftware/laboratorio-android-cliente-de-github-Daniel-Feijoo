package ec.edu.uisek.githubclient

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ec.edu.uisek.githubclient.databinding.FragmentRepoItemBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.R

class ReposViewHolder(
    private val binding: FragmentRepoItemBinding,
    private val onEdit: (Repo) -> Unit,
    private val onDelete: (Repo) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(repo: Repo) {
        binding.repoName.text = repo.name
        binding.repoDescription.text = repo.description ?: "Sin descripción"
        binding.repoLang.text = repo.language ?: "Lenguaje no especificado"

        Glide.with(binding.root.context)
            .load(repo.owner.avatarUrl)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .circleCrop()
            .into(binding.repoOwnerImage)

        binding.editButton.setOnClickListener { onEdit(repo) }
        binding.deleteButton.setOnClickListener { onDelete(repo) }
    }
}

class ReposAdapter(
    private val onEdit: (Repo) -> Unit,
    private val onDelete: (Repo) -> Unit
) : RecyclerView.Adapter<ReposViewHolder>() {

    private var repositories: List<Repo> = emptyList()

    override fun getItemCount(): Int = repositories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReposViewHolder {
        val binding = FragmentRepoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReposViewHolder(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ReposViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRepositories(newRepositories: List<Repo>) {
        repositories = newRepositories
        notifyDataSetChanged()
    }
}