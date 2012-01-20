/******************************** Measure.java ****************************/
/*
 * Utility class for easy measure of time
 * Where does teh time go...?
 */
package my.fabian.rprrdr;

import java.util.LinkedList;
import java.util.List;

public class Measure
{
	private long start_t = 0;
	private long stop_t = 0;
	private String name = null;
	
	public static List<Measure> chain = new LinkedList<Measure>();
	
	public static void StopAll()
	{
		for(Measure m : chain)
			m.Stop();
	}
	public static StringBuilder getAll()
	{
		StringBuilder b = new StringBuilder();
		for(Measure m : chain)
		{
			b.append(m.name).append(": ").append(Long.toString(m.stop_t-m.start_t)).append(", ");;
		}
		return b;
	}
	public static void Clear()
	{
		chain.clear();
	}
	public Measure(final String name)
	{
		this.name = name;
		chain.add(this);
	}
	public Measure Start()
	{
		start_t = System.currentTimeMillis();
		stop_t = 0;
		return this;
	}
	public Measure Stop()
	{
		if(stop_t == 0)	// if stopped several times, only the first one matters
			stop_t = System.currentTimeMillis();
		return this;
	}
	@Override
	public String toString()
	{
		return name + ": " + (stop_t - start_t);
	}
}
