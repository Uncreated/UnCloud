package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Server.Answer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTransfer implements Answer
{
	public static final int PART_SIZE = 10485760;//10mb

	String path;
	Integer part;
	Integer parts;
	byte[] data;

	public FileTransfer()
	{
	}

	public void read(String rootFolder) throws IOException
	{
		FileInputStream inputStream = new FileInputStream(rootFolder + path);
		inputStream.skip(part * PART_SIZE);
		inputStream.read(data, 0, data.length);
		inputStream.close();
	}

	public void write(String rootFolder) throws IOException
	{
		FileOutputStream outputStream = new FileOutputStream(rootFolder + path);
		outputStream.write(data, part * PART_SIZE, data.length);
		outputStream.close();
	}

	public FileTransfer(String path, Integer part, int size)
	{
		this.path = path;
		this.part = part;
		this.parts = getParts(size);
		this.data = new byte[size];
	}

	public String getPath()
	{
		return path;
	}

	public Integer getPart()
	{
		return part;
	}

	public Integer getParts()
	{
		return parts;
	}

	public byte[] getData()
	{
		return data;
	}

	public static int getParts(long size)
	{
		int parts = (int) (size / PART_SIZE);
		if (size % PART_SIZE != 0)
			parts++;
		return parts;
	}

	public static int getSizeOfPart(long fileSize, int part)
	{
		fileSize -= part * PART_SIZE;
		if (fileSize > PART_SIZE)
			fileSize = PART_SIZE;

		return (int) fileSize;
	}
}
