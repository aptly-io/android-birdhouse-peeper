package xyz.techmush.birdhouse_peeper.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.R
import xyz.techmush.birdhouse_peeper.databinding.BirdhouseListFragmentBinding
import xyz.techmush.birdhouse_peeper.databinding.ViewBirdhouseBinding
import xyz.techmush.birdhouse_peeper.model.BirdhouseRepository
import xyz.techmush.birdhouse_peeper.model.BirdhouseRepository.Birdhouse
import xyz.techmush.birdhouse_peeper.vm.BirdhouseListViewModel
import javax.inject.Inject


@AndroidEntryPoint
class BirdhouseListFragment: Fragment() {

    private lateinit var viewModel: BirdhouseListViewModel
    private lateinit var binding: BirdhouseListFragmentBinding
    private lateinit var swipeHelper: ItemTouchHelper
    @Inject lateinit var repository: BirdhouseRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        viewModel = ViewModelProvider(this).get(BirdhouseListViewModel::class.java)
        binding = BirdhouseListFragmentBinding.inflate(inflater, container,false)
        binding.vm = viewModel
        
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.birdhouses.adapter = BirdhouseListAdapter(viewModel)

        swipeHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val holder = viewHolder as BirdhouseListAdapter.ViewHolder
//                repository.delete(holder.binding.birdhouse!!)
//                holder.bindingAdapter!!.notifyItemRemoved(holder.bindingAdapterPosition)
                viewModel.onRemoveBirdhouse(holder.binding.birdhouse!!)
            }
        })
        swipeHelper.attachToRecyclerView(binding.birdhouses)

        viewModel.event.observe(viewLifecycleOwner, { event -> when (event) {
            is BirdhouseListViewModel.Event.AddBirdhouse -> {
                findNavController().navigate(BirdhouseListFragmentDirections
                    .actionBirdhouseListFragmentToBirdhouseFindFragment())
            }
            is BirdhouseListViewModel.Event.PeepBirdhouse -> {
                findNavController().navigate(BirdhouseListFragmentDirections
                    .actionBirdhouseListFragmentToBirdhousePeeperFragment(event.birdhouse.address))
            }
        } })
    }

} // BirdhouseListFragment


class BirdhouseListAdapter(private val viewModel: BirdhouseListViewModel):
    ListAdapter<Birdhouse, BirdhouseListAdapter.ViewHolder>(DiffCallback()) {


    class DiffCallback : DiffUtil.ItemCallback<Birdhouse>() {
        override fun areItemsTheSame(old: Birdhouse, new: Birdhouse) =
            old.address == new.address
        override fun areContentsTheSame(old: Birdhouse, new: Birdhouse) =
            old.address == new.address
    }


    class ViewHolder constructor(val binding: ViewBirdhouseBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(birdhouse: Birdhouse, viewModel: BirdhouseListViewModel, adapter: BirdhouseListAdapter) {

            binding.birdhouse = birdhouse
            binding.vm = viewModel
            binding.executePendingBindings()

            if (viewModel.birdhouse.value?.address == birdhouse.address) {
                binding.root.setBackgroundResource(R.color.purple_200)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

            binding.root.setOnClickListener {
                viewModel.onBirdhouseClick(birdhouse)
                adapter.notifyDataSetChanged() // forced a redraw
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewBirdhouseBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, this)
    }

    fun remove(position: Int) {
        this.currentList.removeAt(position)
        notifyItemRemoved(position);
    }

} // BirdhouseListAdapter


@BindingAdapter("items")
fun setItems(listView: RecyclerView, items: List<Birdhouse>?) {
    items?.let { (listView.adapter as BirdhouseListAdapter).submitList(it) }
}
