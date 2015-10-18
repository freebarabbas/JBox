package clsTypes;

import java.lang.String;

public class chunk implements Comparable<chunk>
{
	public int index;
	public int flag; // bit 1: compress 
	public long start;
	public long end;
	public String hashvalue;
	public chunk(int id, long s, long e, String hv)
	{
		index = id;
		start = s;
		end = e;
		hashvalue = hv;
		flag = 0;
	}
	
	public chunk(int id, int f, long s, long e, String hv)
	{
		index = id;
		start = s;
		end = e;
		hashvalue = hv;
		flag = f;
	}

	public chunk(int id, String input)
	{
		String[] tmp = input.trim().split("\\s*\t\\s*");
		index = id;
		start = Long.parseLong(tmp[1].trim());
		end = Long.parseLong(tmp[2].trim()) + start - 1;
		if(tmp.length==4){
			hashvalue = tmp[3];
			flag=0;
		}
		else
		{
			flag=Integer.parseInt(tmp[3]);
			hashvalue=tmp[4];
		}
	}

	//@Override
	public String toString()
	{
		if (index == 0)
		{
			return "file " + (new Long(start)).toString() + " " + (new Long(end)).toString() + " " + String.valueOf(flag) + " " + hashvalue;
		}
		else
		{
			return "chunk " + (new Long(start)).toString() + " " + (new Long(end)).toString() + " " + String.valueOf(flag) + " " + hashvalue;
		}
	}

	@Override
	public int compareTo(chunk o) {
		return this.index-o.index;
	}
}