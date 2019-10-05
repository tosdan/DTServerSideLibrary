package com.github.tosdan.utils.varie;

import java.util.Random;

public class Rand
{
	public static int getRandom( int aStart, int aEnd, Random aRandom ) {
		if ( aStart > aEnd ) {
			throw new IllegalArgumentException( "Start deve esser minore di End." );
		}
		// get the range, casting to long to avoid overflow problems
		long range = ( long ) aEnd - ( long ) aStart + 1;
		// compute a fraction of the range, 0 <= frac < range
		long fraction = ( long ) ( range * aRandom.nextDouble() );
		
		return ( int ) ( fraction + aStart );
	}
}
