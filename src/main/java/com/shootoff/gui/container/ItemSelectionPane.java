package com.shootoff.gui.container;

import java.util.HashMap;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shootoff.gui.container.listeners.ItemSelectionListener;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.TilePane;
import javafx.scene.text.TextAlignment;

public class ItemSelectionPane extends ScrollPane {
	private static final Logger logger = LoggerFactory.getLogger(ItemSelectionPane.class);

	
	private Boolean toggleable = false;
	
	private static final int COLUMNS = 6;
	private static final int ITEM_DIMS = 150;
	
	private HashMap<Object, ButtonBase> items = new HashMap<Object, ButtonBase>();
	
	
	private final TilePane subContainer = new TilePane(30, 30);
	
	private ToggleGroup toggleGroup = null;
	private Object defaultSelection = null;
	private Object currentSelection = null;

	
	private ItemSelectionListener itemListener = null;
	
	public ItemSelectionPane(boolean toggleItems, ItemSelectionListener itemListener)
	{
		super();
	
		this.itemListener = itemListener;
		this.toggleable = toggleItems;
		
		if (toggleable)
		{
			toggleGroup = new ToggleGroup();
		}
		
		subContainer.setPrefColumns(COLUMNS);
		subContainer.setPadding(new Insets(0, 65, 65, 65));

		this.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-color:transparent;");
		this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		this.setHbarPolicy(ScrollBarPolicy.NEVER);
		this.setFitToHeight(true);
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setContent(subContainer);
		
	}
	
	public void addButton(Object ref, String text, Optional<Node> graphic, Optional<Tooltip> tooltip)
	{
		final ButtonBase button;
		if (toggleable)
		{
			if (defaultSelection == null)
				defaultSelection = ref;
			
			button = new ToggleButton(text);
			
			((ToggleButton) button).setToggleGroup(toggleGroup);
			
			
		}
		else
		{
			button = new Button(text);
		}
		
		button.setContentDisplay(ContentDisplay.TOP);
		button.setTextAlignment(TextAlignment.CENTER);
		button.setPrefSize(ITEM_DIMS, ITEM_DIMS);
		button.setWrapText(true);
		
		if (graphic.isPresent())
			button.setGraphic(graphic.get());
		if (tooltip.isPresent())
			button.setTooltip(tooltip.get());

		button.setOnAction((event) -> {
			itemListener.onItemClicked(ref);
			
			if (toggleable)
				toggleGroup.selectToggle((Toggle) button);
		});
		
		subContainer.getChildren().add(button);
		
		items.put(ref, button);
		
	}
	
	public void addButton(Object ref, String text) {
		this.addButton(ref, text, Optional.empty(), Optional.empty());
	}
	
	public void setDefault(Object ref)
	{
		if (!toggleable)
		{
			logger.error("setDefault only applies to toggleable item selection");
			return;
		}
		
		if (items.containsKey(ref))
		{
			if (defaultSelection == null && currentSelection == null)
			{
				currentSelection = ref;
				toggleGroup.selectToggle((Toggle) items.get(ref));
			}
			defaultSelection = ref;
		}
		else
			logger.error("setDefault on non-existing ref - %s", ref);
	}
	
	public void removeButton(Object ref)
	{
		if (!items.containsKey(ref))
		{
			logger.error("removeButton on non-existing ref - %s", ref);
			return;
		}
		
		Node item =	items.remove(ref);
		
		Platform.runLater(() -> {
			subContainer.getChildren().remove(item);
		});
		
		if (toggleable && ref == defaultSelection)
		{
			defaultSelection = null;
		}
		if (toggleable && ref == currentSelection)
		{
			if (defaultSelection != null)
			{
				currentSelection = defaultSelection;
				itemListener.onItemClicked(currentSelection);
				toggleGroup.selectToggle((Toggle) items.get(currentSelection));
			}
		}
	}
		
	public Object getCurrentSelection()
	{
		return currentSelection;
	}
	
	public void setSelection(Object ref)
	{
		if (items.containsKey(ref))
		{
			currentSelection = ref;
			toggleGroup.selectToggle((Toggle) items.get(currentSelection));
		} else {
			logger.error("setSelection on non-existing ref - %s", ref);
		}
	}


}
