package com.moe.trie.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * An Implementation of Double-Array Trie
 * @author songwenjun
 * See more at:http://linux.thai.net/~thep/datrie/datrie.html
 */
public class DATrie {
	private int[] base = new int[16];
	private int[] check=new int[16];
	private int state =0;
	private static final char[] wordTable={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	private static final char[] symbolTable={' ','-','.','/'};
	private static TreeMap<Character,Integer> chartoint=new TreeMap<Character,Integer>();
	private static TreeMap<Integer, List<Integer>> checkMap=new TreeMap<Integer,List<Integer>>();
	private static TreeMap<Integer,Integer> baseMap=new TreeMap<Integer,Integer>();
	private int nbase=0;
	public DATrie(){
		init();
	}
	
	private void init(){		
		for(int i=0;i<wordTable.length;i++){
			chartoint.put(wordTable[i], i+1);			
		}
		for(int i=0;i<symbolTable.length;i++){		
			int value=chartoint.get(wordTable[wordTable.length-1]).intValue();
			chartoint.put(symbolTable[i], value+i+1);		
		}		
		check[0]=-1;
	}
	
	private void addWord(String word){			
		for(int i=0;i<word.length();i++){
			addtoTrie(word.charAt(i));
		}
	}
	
	private void addtoTrie(char character){	
		int  next = base[state]+chartoint.get(character);
		if(next>base.length-1)
			ensureCapacity(next+1);
			if(check[next]==state){
				state=next;
			}else{
				if(check[next]>0){
					nbase=newbase(nbase);
					while(relocate(state,nbase)==0){
						nbase=newbase(nbase);
					}
					addtoTrie(character);					
				}else if(check[next]==0){
					check[next]=state;						
					addtoCheckMap(state,chartoint.get(character));			
					state=next;
					base[state]=next;
				}else{
					throw new RuntimeException("what's wrong! ");
				}
			}
	}
	
	private void addtoCheckMap(int state,int charc){
		List<Integer> list =checkMap.get(state);
		if(list==null)
			list = new ArrayList<Integer>();		
		list.add(charc);
		checkMap.put(state,list);
	}
	
	private void removefromCheckMap(int state){
		if(checkMap.containsKey(state))
			checkMap.remove(state);
	}
	
	private int nextbase(int start){
		int nb=0;
		for(int i=start;i<base.length;i++){
			if(base[i]==0&&check[i]==0){
				nb=i;
				break;
			}
		}
		return nb;
	}
	
	private int newbase(int start){
		int avabase=nextbase(start+1);
		if(avabase==0){
			ensureCapacity(base.length+1);
			avabase=base.length;
		}
		return avabase;
	}
	
	private int relocate(int state,int new_base){		
		List<Integer> input_state=inputchars(state);		
		for(int n=0;n<input_state.size();n++){				
			int c =input_state.get(n);			
			int ns=new_base+c;
			if(ns>=base.length)
				continue;
			if(base[ns]!=0||check[ns]!=0)
				return 0;
		}
		for(int i=0;i<input_state.size();i++){			
			int c =input_state.get(i);				
			ensureCapacity(Math.max(new_base+c, base[state]+c)+1);			
			check[new_base+c]=state;
			base[new_base+c]=base[base[state]+c];			
			List<Integer> input_next= inputchars(base[state]+c);
			for(int j=0;j<input_next.size();j++){				
				int d=input_next.get(j);				
				ensureCapacity(base[base[state]+c]+d+1);
				check[base[base[state]+c]+d]=new_base+c;				
				addtoCheckMap(new_base+c,d);				
			}
			removefromCheckMap(base[state]+c);
			check[base[state]+c] = 0;		
		}
		base[state] = new_base;
		return 1;
	}
	
	private void preprocess(){
		state=1;
		nbase=0;	
	}
	
	private List<Integer> inputchars(int state){			
		List<Integer> indexes=checkMap.get(state);
		if(indexes==null)
			indexes = new ArrayList<Integer>();
		return indexes;
	}
	
	private void ensureCapacity(int minCapacity) { 
        int capacity = base.length; 
        if (minCapacity > capacity) { 
              int newCapacity = (capacity * 3) / 2 + 1; 
              if (newCapacity < minCapacity) 
                    newCapacity = minCapacity; 
              base = Arrays.copyOf(base, newCapacity); 
              check=Arrays.copyOf(check, newCapacity); 
        } 
  } 

	
	private void addWords(File dicfile) throws IOException{
		BufferedReader in =null;
		try{
				in= new BufferedReader(new FileReader(dicfile));
				String word =null;
				word=in.readLine();	
				while(word!=null){
					preprocess();
					addWord(word);			 
					word=in.readLine();			
				}
		}finally{
			if(in!=null)
			 in.close();
		}
		
	}
	
	
	private void printTrie(String outfile) throws IOException{
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {			
			 fos = new FileOutputStream(outfile); 
	         osw = new OutputStreamWriter(fos, "UTF-8"); 
			for(int i=0;i<base.length;i++){
				osw.write(base[i]+"\t");
				osw.write(String.valueOf(check[i]));
				osw.write("\r\n");				
			}	        
	        osw.flush(); 
		} finally{
			if(fos!=null)
				fos.close();
			if(osw!=null)
				osw.close();
		}
	}
	
	private boolean within(String word){
		boolean flag =false;
		preprocess();
		for(int i=0;i<word.length();i++){
			char ch=word.charAt(i);
			int  next = base[state]+chartoint.get(ch);
			if(next>base.length-1)
				throw new RuntimeException("out of range!");
			if(check[next]==state)
				state=next;
			else
				break;
			if(i==word.length()-1)
				flag=true;
		}
		return flag;
	}
	
	private void debug(File dicfile){
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(dicfile));
			String word =null;
			word=in.readLine();			
			 while(word!=null){
				 if(!within(word)){
					 System.out.println(word+"  is not in the trie!");
					 break;
				 }
				 word=in.readLine();			
			 }		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws Exception {
		DATrie instance = new DATrie();
		instance.addWords(new File("d:/dic.txt"));
		//instance.printTrie("d:/trie.txt");
		//instance.debug(new File("d:/dic.txt"));
		long start = System.currentTimeMillis();
		String word = "frightening";
		System.out.println(instance.within(word));
		System.out.println(System.currentTimeMillis() - start); 
	}
}
