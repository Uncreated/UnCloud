package com.uncreated.uncloud.client.files.view;

import com.uncreated.uncloud.client.View;
import com.uncreated.uncloud.client.files.FileInfo;

import java.util.ArrayList;

public interface FilesView
		extends View
{
	void showFolder(ArrayList<FileInfo> files, boolean rootFolder);

	void onFailRequest(String message);

	void onProgress(int value, int max, String unit);
}
