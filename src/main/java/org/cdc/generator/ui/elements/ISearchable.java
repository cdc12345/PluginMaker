package org.cdc.generator.ui.elements;

import java.util.Map;

public interface ISearchable {
	void doSearch(Map.Entry<String,String> search);

	void refreshTable();

	void showSearch(int index);
}
