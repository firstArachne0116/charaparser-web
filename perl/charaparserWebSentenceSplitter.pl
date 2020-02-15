use SentenceSpliter;
use utf8;
use open ':std', ':encoding(UTF-8)'; #to prevent "wide character" warnings when print debug info.

my $text = $ARGV[0];
my $N = 3; #$N leading words
my $connectors = "and|or|plus|to|sometimes";
my %WORDS = ();my $PREPOSITION ="above|across|after|along|among|amongst|around|as|at|before|behind|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out|outside|over|per|than|through|throughout|toward|towards|up|upward|with|without";
my $NUMBERS = "zero|one|ones|first|two|second|three|third|thirds|four|fourth|fourths|quarter|five|fifth|fifths|six|sixth|sixths|seven|seventh|sevenths|eight|eighths|eighth|nine|ninths|ninth|tenths|tenth";
my %WNNUMBER =(); #word->(p|s)
my %WNSINGULAR = ();#word->singular
my %WNPOS = ();   #word->POSs
my %WNPOSRECORDS = ();
my $debug = 0;
my $PREFIX ="ab|ad|bi|deca|de|dis|di|dodeca|endo|end|e|hemi|hetero|hexa|homo|infra|inter|ir|macro|mega|meso|micro|mid|mono|multi|ob|octo|over|penta|poly|postero|post|ptero|pseudo|quadri|quinque|semi|sub|sur|syn|tetra|tri|uni|un|xero|[a-z0-9]+_";


my (@sentences,@words,@tmp,$status,$lead,$stmt,$sth, $escaped, $original, $count);
#print STDOUT "original text: $text\n";
$text =~ s#["']##g;
$text =~ s#[-_]+shaped#-shaped#g; #5/30/09
$text =~ s#<[^ ]*>##g; #remove html tags (at least those without space for now. Real example text can contain < > e.g. leaves < 4; sometimes > 5mm tall)
$text =~ s#<# less than #g; #remove <
$text =~ s#># greater than #g; #remove >
$text =~ s#(?<=[a-zA-Z])([)}\]])(?=[a-zA-Z])#$1 #g; # a)a => a) a purple)ovate-
$text =~ s#(?<=[a-zA-Z])([(\[{])(?=[a-zA-Z])# $1#g; # a(a => a (a
#normalize $line: "plagio-, dicho-, and trichotriaenes" => "plagiotriaenes, dichotriaenes, and trichotriaenes"
#$text = normalizeBrokenWords($text); #it is not quite 'original' anymore with this normalization, but it is convenient to do it here without having to change a lot of other code. 
#print STDOUT "text 2: $text\n";                                

$text =~ s#^\s*\d+[a-z]\.\s*##; #remove 2a. (key marks)
#print STDOUT "now text: $text\n";
$original = $text;
$text =~ s#\b(is|are|was|were|be|being)\b##g; #remove aux.verbs
#$text =~ s#\b[Nn]o\s*\.\s*(?=\d+)#taxonname#g; #similar to No. 12
$text =~ s#[-]+#-#g; #-- => -
$text =~ s#\+/-# moreorless #g;

#$text =~ s/&[;#\w\d]+;/ /g; #remove HTML entities
$text =~ s/&[#\w\d]+;/ /g; #remove HTML entities, &lt;; the 2nd ; is a punc mark.
#print STDOUT "text 3: $text\n";  

$text =~ s# & # and #g;
$text = hideBrackets($text);#implemented in DeHyphenAFolder.java
$text =~ s#_#-#g;   #_ to -
$text =~ s#\s+([:;\.])#\1#g;     #absent ; => absent;
$text =~ s#(\w)([:;\.])(\w)#$1$2 $3#g; #absent;blade => absent; blade
$text =~ s#(\d\s*\.)\s+(\d)#$1$2#g; #1 . 5 => 1.5
$text =~ s#(\sdiam)\s+(\.)#$1$2#g; #diam . =>diam.
$text =~ s#(\sca)\s+(\.)#$1$2#g;  #ca . =>ca.
$text =~ s#(\d\s+(cm|mm|dm|m)\s*)\.(\s+[^A-Z])#$1\[DOT\]$3#g;
#print "end text: $text\n";
  	

#@todo: use [PERIOD] replace . etc. in brackets. Replace back when dump to disk.
@sentences = SentenceSpliter::get_sentences($text);#@todo: avoid splits in brackets. how? use hideBrackets.
#my @sentcopy = @sentences;
my @sentcopy = ();
my @validindex = ();
my $i = 0;
foreach (@sentences){
	#may have fewer than $N words
	if(!/\w+/){next;}
	#print STDOUT "sent: $_\n";
	push(@validindex, $i);
	s#\[\s*DOT\s*\]#.#g;
	s#\[\s*QST\s*\]#?#g;
	s#\[\s*SQL\s*\]#;#g;
	s#\[\s*QLN\s*\]#:#g;
	s#\[\s*EXM\s*\]#!#g;
	$_ = normalizeBrokenWords($_);			  #do this before getallwords and creates @sentcopy (for context searching). Spacing around punct marks is of the standard English style.
	push(@sentcopy, $_);

	#remove bracketed text from sentence (keep those in originalsent);
	#this step will not be able to remove nested brackets, such as (petioles (2-)4-8 cm).
	#nested brackets will be removed after threedsent step in POSTagger4StanfordParser.java
  	s#\([^()]*?[a-zA-Z][^()]*?\)# #g;  #remove (.a.)
  	s#\[[^\]\[]*?[a-zA-Z][^\]\[]*?\]# #g;  #remove [.a.]
  	s#{[^{}]*?[a-zA-Z][^{}]*?}# #g; #remove {.a.}
	#print STDOUT "sent2: $_\n";
    #s#([^\d])\s*-\s*([^\d])#\1_\2#g;         #hyphened words: - =>_ to avoid space padding in the next step
	s#([^/])[-]+\s*([a-z])#$1_$2#g;                #cup_shaped, 3_nerved, 3-5 (-7)_nerved #5/30/09 add+, exclude +/- hairy 
	s#(\W)# \1 #g;  		#add space around nonword char
	#print STDOUT "sent3: $_\n";
    #s#& (\w{1,5}) ;#&\1;#g;
    s#\s+# #g;                                #multiple spaces => 1 space
    s#^\s*##;                                 #trim
    s#\s*$##;                                 #trim
    #recordpropernouns($_);
    tr/A-Z/a-z/;                              #all to lower case
    getallwords($_);
    $i++;
}

$count = 0;
my $beforeColon = "";
#foreach (@sentences){
foreach (@validindex){
	#may have fewer than $N words
	#if(!/\w+/){next;}
	#my $line = $_;
	#my $oline = getOriginal($line, $original, $file);

    my $line = $sentences[$_];	
	#print stdout "Sentence ".$count.": $line\n";
	#print STDOUT "$SENTID 1\n";
    my $oline = $sentcopy[$_];
    $oline =~ s#(\d)\s*\[\s*DOT\s*\]\s*(\d)#$1.$2#g;
    $oline =~ s#\[\s*DOT\s*\]#.#g; #a space may have been introduced
	$oline =~ s#\[\s*QST\s*\]#?#g;
	$oline =~ s#\[\s*SQL\s*\]#;#g;
	$oline =~ s#\[\s*QLN\s*\]#:#g;
	$oline =~ s#\[\s*EXM\s*\]#!#g;
    $line =~ s#'# #g; #remove all ' to avoid escape problems	
    $oline =~ s#'# #g;
	#print STDOUT "$SENTID 2\n";
		
	#normalize $line: "plagio - , dicho - , and trichotriaenes" => "plagiotriaenes , dichotriaenes , and trichotriaenes"
    #$line = normalizeBrokenWords($line); #after allwords have been collected
		
    @words = getfirstnwords($line, $N); # "w1 w2 w3"
	#print STDOUT "$SENTID 3\n";
    $status = "";
	if(getnumber($words[0]) eq "p"){
	     $status = "start";
	}else{
	     $status = "normal";
	}
	#print STDOUT "$SENTID 4\n";		
	$lead = "@words";
	$lead =~ s#\s+$##;
	$lead =~ s#^\s*##;
	$lead =~ s#\s+# #g;
    #s#"#\\"#g;
    #s#'#\\'#g;

    #s#\(#\\(#g;
    #s#\)#\\)#g;
	#print STDOUT "$SENTID 5\n";
    my $source = $file."-".$count++;
    if(length($oline) >=2000 ){#EOL
    	$oline = $line;
    }
	#print STDOUT "$SENTID 6\n";
    #checked in DeHyenAFolder.java
	#if(hasUnmatchedBrackets($oline)){
	#	print STDOUT "Warning: sentence [id = $SENTID] has unmatched brackets\n";
	#}
		
	#Length of tibia/metatarsus: leg I, 0.40/0.30 mm; leg II, 0.34/0.34 mm; leg III, 0.22/0.20 mm; leg IV, 0.32/0.20 mm.
	# => Length of tibia/metatarsus: leg I, 0.40/0.30 mm; Length of tibia/metatarsus: leg II, 0.34/0.34 mm; Length of tibia/metatarsus: leg III, 0.22/0.20 mm; Length of tibia/metatarsus: leg IV, 0.32/0.20 mm.

		
	if($beforeColon=~/\w{3,}/ && $line !~/:/){ #is equal or longer than 3 characters, to exclude 1:, 12: and a: bullet points, and also doesn't contain :
		$line = $beforeColon.": ".$line;
	}
	print STDOUT "$line\n";
}


#hide [\.\?\;\:\!] if they are in brackets
sub hideBrackets{
	my $text = shift;
	$text =~ s/([\(\)\[\]\{\}])/ \1 /g;

	my $lround=0;
	my $lsquare=0;
	my $lcurly=0;

	my $hidden="";

	my @tokens = split(/[\s]+/, $text);

	foreach (@tokens){
		if($_ eq "("){
			$lround++;
			$hidden .= "(";	
		}elsif($_ eq ")"){
			$lround--;
			$hidden .= ") ";
		}elsif($_ eq "["){
			$lsquare++;
			$hidden .= "[";
		}elsif($_ eq "]"){
			$lsquare--;
			$hidden .= "] ";
		}elsif($_ eq "{"){
			$lcurly++;
			$hidden .= "{";
		}elsif($_ eq "}"){
			$lcurly--;
			$hidden .= "} ";
		}else{
			if($lround+$lsquare+$lcurly>0){
				if(/.*?[\.\?\;\:\!].*?/){
					s/\./\[DOT\]/g;
					s/\?/\[QST\]/g;
					s/\;/\[SQL\]/g;
					s/\:/\[QLN\]/g;
					s/\!/\[EXM\]/g;
				}
			}
			$hidden .= $_;
			$hidden .= " ";
		}
	}	
	$hidden =~ s/([\(\[\{]\s+)/$1/g;
	$hidden =~ s/\s+([\)\]\}])/$1/g;
	return $hidden;
}

sub normalizeBrokenWords{
	
	my $line = shift;
	my $cline = $line;
	$line =~ s#([(\[{])(?=[a-zA-Z])#$1 #g; #add space to () that enclose text strings (not numbers such as 3-(5))
	$line =~ s#(?<=[a-zA-Z])([)\]}])# $1#g;
	my $result = "";
	my $needsfix = 0;
	while($line=~/(.*?\b)((\w+\s*-\s*\)?,.*?\b)((?:$connectors)\s+.*))/ || $line=~/(.*?\b)((\w+\s*-\s+)((?:$connectors)\s+.*))/){
		my @completed = completeWords($2, $3, $4);
		$result .= $1.$completed[0]." ";
		$line = $completed[1];
		$needsfix = 1;	
	}
	$result .= $line;
	$result =~ s#\s+# #g;
	$result =~ s#([(\[{])\s+#$1#g;
	$result =~ s#\s+([)\]},;\.])#$1#g;
	$result =~ s#(^\s+|\s+$)##g; #trim
	$cline =~ s#(^\s+|\s+$)##g; #trim
	#if($needsfix and $cline ne $result){
	#	 print STDOUT "broken words normalization: [$cline] to \n";
	#	 print STDOUT "broken words normalization: [$result] \n";
	#};
	return $result;
}

sub getallwords{
  my $sentence = shift;
  my @words = tokenize($sentence, "all");
  foreach my $w (@words){
    $WORDS{$w}++;
  }
}

sub tokenize{
    my ($sentence, $mode) = @_;
	my ($index, @words, $temp1, $temp2);
    if($mode ne "all"){
    	$temp1 = length($sentence); #3/11/09
    	$temp2 = $temp1;#
    	if($sentence =~ / [,:;\.\[(]/){
			$temp1 = $-[0];
			#$index = $temp1;#
		}
		if($sentence =~ /\b(?:$PREPOSITION)(\s)/){#3/1109
			$temp2 = $-[1];
		}
		$index = $temp1 < $temp2? $temp1 : $temp2;#
	}else{
		$index = length($sentence);
	}
	$sentence = substr($sentence, 0, $index);

	#$sentence =~ s#[[:punct:]]# #g; #remove all punct. marks
	#$sentence =~ s#\W# #g; #keep punctuation marks in $lead
	$sentence =~ s#(\(? ?\d[\d\W]*)# NUM #g;
	$sentence =~ s#\b($NUMBERS|_)+\b# NUM #g; #3/12/09
	$sentence =~ s#\s+$##;
	$sentence =~ s#^\s+##;
	@words = split(/\s+/, $sentence);
  	return @words;
}

sub getfirstnwords{
	########return the first up to $n words of $sentence as an array, excluding
	my($sentence, $n) = @_;
	my (@words, $index, $w);
	@words = tokenize($sentence, "firstseg");
	#print "words in sentence: @words\n" if $debug;
	@words = splice(@words, 0, $n);
	return @words;
}

sub getnumber{
  my $word = shift;
  #$word = lc $word;
  my $number = checkWN($word, "number");
  #print STDOUT "$SENTID 3 done\n";
  return $number if $number =~/[sp]/;
  return "" if $number=~/x/;
  if($word =~/i$/) {return "p";} #1.	Calyculi  => 1.	Calyculus, pappi => pappus
  if ($word =~ /ss$/){return "s";}
  if($word =~/ia$/) {return "p";}
  if($word =~/[it]um$/) {return "s";}#3/13/09
  if ($word =~/ae$/){return "p";}
  if($word =~/ous$/){return ""; }
  if($word =~/^[aiu]s$/){return ""; }
  if ($word =~/us$/){return "s";}
  if($word =~ /es$/ || $word =~ /s$/){return "p";}
  if($word =~/ate$/){return "";} #3/12/09 good.
  return "s";
}

sub checkWN{
  my ($word, $mode) = @_;
  #$word = lc $word;
  #check saved records
  $word =~ s#\W##g; #remove non-word characters, such as <>
  return "" if $word eq "";
  my $singular = $WNSINGULAR{$word} if $mode eq "singular";
  return $singular if $singular =~ /\w/;
  my $number = $WNNUMBER{$word} if $mode eq "number";
  return $number if $number =~ /\w/;
  my $pos = $WNPOS{$word} if $mode eq "pos";
  return $pos if $pos =~/\w/;

  #special cases
  if ($word eq "teeth"){
    $WNNUMBER{"teeth"} = "p";
    $WNSINGULAR{"teeth"} = "tooth";
    return $mode eq "singular"? "tooth" : "p";
  }

  if ($word eq "tooth"){
    $WNNUMBER{"tooth"} = "s";
    $WNSINGULAR{"tooth"} = "tooth";
    return $mode eq "singular"? "tooth" : "s";
  }

  if ($word eq "NUM")
  {
    return $mode eq "singular"? "NUM" : "s";
  }

  if ($word eq "or"){
    return $mode eq "singular"? "or" : "";
   }

   if ($word eq "and"){
    return $mode eq "singular"? "and" : "";
  }

  if ($word =~ /[a-z]{3,}ly$/){#concentrically
    return $word if $mode eq "singular";
    return "" if $mode eq "number";
    return "r" if $mode eq "pos";
  }
#print STDOUT "$SENTID 3.1\n";
  #otherwise, call wn
  my $result = `wn $word -over`;
#print STDOUT "$SENTID 3.2 $word: *$result*\n";  
  if($result =~ /not recognized/ and $result!~/overview/i){	#capture 'command not recognized error'
		print STDOUT "$result:\n";
		print STDOUT "Please make sure WordNet is properly installed and try again\n".
		exit(1);
  }
#print STDOUT "$SENTID 3.3\n";
  if ($result !~/\w/){#word not in WN
#print STDOUT "$SENTID 3.4: *$word*\n";
  	$WNPOSRECORDS{$word} = ""; #5/10/09
#print STDOUT "$SENTID 3.5\n";
  	#return $mode eq "singular"? $word : ""; #not in WN, return ""
  	#remove any prefix and try again 3/12/09
  	my $wordcopy = $word;
#print STDOUT "$SENTID 3.5.5\n";
  	$word =~ s#ed$##;
#print STDOUT "$SENTID 3.5.6 *$word*\n";
  	if($word ne $wordcopy){ #$word not end with "ed"
  		$result = `wn $word -over`;
#print STDOUT "$SENTID 3.5.7 *$result*\n";
  		if($result =~ /\w/){ #$word end with "ed", what remains after removes "ed" is still a word
  			return $word if $mode eq "singular";
  			return "" if $mode eq "number";
  			return "a" if $mode eq "pos";
  		}
#print STDOUT "$SENTID 3.6\n";
  	}
  	$word = $wordcopy;
  	$word =~ s#^($PREFIX)+##;
#print STDOUT "$SENTID 3.7\n";  	
  	if($word eq $wordcopy){
  		return $mode eq "singular"? $word : ""; #not in WN, return ""
  	}else{
  		$result = `wn $word -over`;
  		$result =~ s#\b$word\b#$wordcopy#g;
  		$word = $wordcopy;
  		return $mode eq "singular"? $word : "" if ($result !~/\w/);
  	}
#print STDOUT "$SENTID 3.8\n";
  }

  #found $word in WN:
  $result =~ s#\n# #g;
  if($mode eq "singular" || $mode eq "number"){
    my $t = "";
    while($result =~/Overview of noun (\w+) (.*) /){
         $t .= $1." ";
         $result = $2;
    }
    if ($t !~ /\w/){#$word is not a noun
    	#return "v";
    	return $mode eq "singular"? $word : "x"; #is not a noun, return "x"
    }
    $t =~ s#\s+$##;
    my @ts = split(/\s+/, $t);
    ###select the singular between roots and root.   bases => basis and base?
    if(@ts > 1){
      my $l = 100;
      print "Word $word has singular\n" if $debug;
      foreach (@ts){
       print "$_\n" if $debug;
       # -um => a, -us => i?
       if (length $_ < $l){
          $t = $_;
          $l = length $_;
       }
      }
      print "The singular is $t\n" if $debug;
    }
    if ($t ne $word){
       $WNSINGULAR{$word} = $t;
       $WNNUMBER{$word} = "p";
       return $mode eq "singular"? $t : "p";
    }else{
       $WNSINGULAR{$word} = $t;
       $WNNUMBER{$word} = "s";
       return $mode eq "singular"? $t : "s";
    }
 }elsif($mode eq "pos"){
   my $pos = "";
   while($result =~/.*?Overview of ([a-z]*) (.*)/){
         my $t = $1;
         $result = $2;
         $pos .= "n" if $t eq "noun";
         $pos .= "v" if $t eq "verb";
         $pos .= "a" if $t eq "adj";
         $pos .= "r" if $t eq "adv";

    }
    $WNPOSRECORDS{$word}=$pos;
    if($pos =~/n/ && $pos =~/v/ && $word=~/(ed|ing)$/){ #appearing is a nv, but set it v
    	$pos =~ s#n##g;
    }
    $WNPOS{$word} = $pos;
    print "Wordnet Pos for $word is $pos\n" if $debug;
    return $pos;
  }
 }