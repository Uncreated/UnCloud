package com.uncreated.uncloud.Client;

import com.uncreated.uncloud.Server.RequestException;
import com.uncreated.uncloud.Server.auth.Session;
import com.uncreated.uncloud.Server.storage.FileInfo;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import com.uncreated.uncloud.Server.storage.UserFiles;

public class ClientApp
{
	public static void main(String[] args)
	{
		Controller controller = new Controller();

		try
		{
			controller.register("masha", "siski");
		} catch (RequestException e)
		{
			e.printStackTrace();
		}

		try
		{
			Session session = controller.auth("masha", "siski");

			try
			{
				UserFiles userFiles = controller.files();
				FileInfo[] files =  userFiles.getFiles();
				FileInfo fileInfo = files[0];
				try
				{
					FileTransfer fileTransfer = controller.getFile(fileInfo.getPath(), 0);
					try
					{
						controller.removeFile(fileInfo.getPath());
						try
						{
							controller.setFile(fileTransfer);
						} catch (RequestException e)
						{
							e.printStackTrace();
						}
					} catch (RequestException e)
					{
						e.printStackTrace();
					}
				} catch (RequestException e)
				{
					e.printStackTrace();
				}
			} catch (RequestException e)
			{
				e.printStackTrace();
			}
		} catch (RequestException e)
		{
			e.printStackTrace();
		}
	}
}
