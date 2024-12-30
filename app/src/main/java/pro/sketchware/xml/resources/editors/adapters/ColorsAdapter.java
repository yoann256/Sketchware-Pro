package pro.sketchware.xml.resources.editors.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.stream.Collectors;

import pro.sketchware.xml.resources.editors.models.ColorItem;
import pro.sketchware.databinding.PalletCustomviewBinding;
import pro.sketchware.utility.PropertiesUtil;
import pro.sketchware.xml.resources.editors.ResourcesEditorActivity;
import pro.sketchware.xml.resources.editors.fragments.ColorEditor;

public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ViewHolder> {

    private final ArrayList<ColorItem> originalData;
    private ArrayList<ColorItem> filteredData;
    private final ResourcesEditorActivity activity;

    public ColorsAdapter(ArrayList<ColorItem> filteredData, ResourcesEditorActivity activity) {
        this.originalData = filteredData;
        this.filteredData = new ArrayList<>(filteredData);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ColorsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PalletCustomviewBinding itemBinding = PalletCustomviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ColorItem colorItem = filteredData.get(position);
        String colorName = colorItem.getColorName();
        String colorValue = colorItem.getColorValue();
        String valueHex = ColorEditor.getColorValue(activity.getApplicationContext(), colorValue, 4);

        holder.itemBinding.title.setHint(colorName);
        holder.itemBinding.sub.setText(colorValue);

        if (PropertiesUtil.isHexColor(valueHex)) {
            holder.itemBinding.color.setBackgroundColor(PropertiesUtil.parseColor(valueHex));
        }

        holder.itemBinding.backgroundCard.setOnClickListener(v -> activity.colorsEditor.showColorEditDialog(colorItem, position));
        holder.itemBinding.backgroundCard.setOnLongClickListener(v -> {
            activity.colorsEditor.showDeleteDialog(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void filter(String newText) {
        if (newText == null || newText.isEmpty()) {
            filteredData = new ArrayList<>(originalData); // Reset to original data
        } else {
            String filterText = newText.toLowerCase().trim();
            filteredData = originalData.stream()
                    .filter(item -> item.getColorName().toLowerCase().contains(filterText) ||
                            item.getColorValue().toLowerCase().contains(filterText))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public PalletCustomviewBinding itemBinding;

        public ViewHolder(PalletCustomviewBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }
}
