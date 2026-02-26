package org.cdc.generator.ui.elements;

public interface ISearchable {
	void doSearch(String text);

	void refreshTable();

	void showSearch(int index);
}
