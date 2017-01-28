package net.metafusion.ptburn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PB
{
	public static void main(String[] args)
	{
		List blue = new ArrayList();
		List red = new ArrayList();
		for (int i = 0; i < 55; i++)
			blue.add(i + 1);
		for (int i = 0; i < 42; i++)
			red.add(i + 1);
		for (int i = 0; i < 10; i++)
		{
			Collections.shuffle(blue);
			Collections.shuffle(red);
			Collections.shuffle(blue);
			Collections.shuffle(red);
			Collections.shuffle(blue);
			Collections.shuffle(red);
			Collections.shuffle(blue);
			Collections.shuffle(red);
			Collections.shuffle(blue);
			Collections.shuffle(red);
			for (int j = 0; j < 5; j++)
				System.out.print("" + blue.get(j) + " ");
			System.out.println("");
			System.out.println("PB:" + red.get(0));
		}
	}
}