package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Server.Answer;

import java.io.*;

public class FileTransfer implements Answer
{
	public static final int PART_SIZE = 10485760;//10mb

	String path;
	Integer part;
	byte[] data;

	public FileTransfer()
	{
	}

	public void read(String rootFolder) throws IOException
	{
		read(new File(rootFolder + path));
	}

	public void read(File file) throws IOException
	{
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
		randomAccessFile.seek((long)part * (long)PART_SIZE);
		randomAccessFile.read(data);
		randomAccessFile.close();
	}

	public void write(String rootFolder) throws IOException
	{
		write(new File(rootFolder + path));
	}

	public void write(File file) throws IOException
	{
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		randomAccessFile.seek((long)part * (long)PART_SIZE);
		randomAccessFile.write(data);
		randomAccessFile.close();
	}

	public FileTransfer(String path, Integer part, int size)
	{
		this.path = path;
		this.part = part;
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
