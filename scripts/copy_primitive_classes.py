#!/usr/bin/python
#
# Example usage:
# find ./src/ilog/cplex/ | xargs -n 1 python ./scripts/cleanup.py

import sys
import re
import os

class TypeDef:
    
    def __init__(self, name, prim, const, is_integral):
        self.name = name
        self.prim = prim
        self.const = const
        self.is_integral = is_integral
        
def get_typedef_repls(src, dest):
    return [(src.name, dest.name),
            (src.prim, dest.prim),
            (src.const, dest.const)]
    
def get_typedefs():
    return {"short" : TypeDef("Short", "short", "SHORT", True),
            "int" : TypeDef("Int", "int", "INT", True),
            "long" : TypeDef("Long", "long", "LONG", True),
            "float" : TypeDef("Float", "float", "FLOAT", False),
            "double" : TypeDef("Double", "double", "DOUBLE", False),
            "none" : TypeDef("", "", "", False),
        }
        
def file_to_str(filename):
    # Read the string from a file.
    f = open(filename, 'r')
    s = ""
    for line in f:
        s = s + line
    return s
    
def str_to_file(s, filename):
    # Write the new string to that file
    f = open(filename, 'w')
    f.write(s)
    f.close()
    
def replace_all(repls, s):
    for k,v in repls:
        s = s.replace(k, v)
    return s

def re_sub_all(re_subs, s):
    for k,v in re_subs:
        s, num_subs = re.subn(k, v, s, flags=re.DOTALL)
        print "Number of substitutions for %s-->%s: %d" % (k, v, num_subs)
    return s

def get_re_subs_for_single(src_prim, dest_prim):
    '''Gets regular expression pairs for use in re.subn (called by re_sub_all). 
        For use when a single source document, is copied and a single primative type 
        is replaced.'''
    repls = []
    add_re_subs = []
    
    repls += get_typedef_repls(src_prim, dest_prim)
    
    # Convert repls to regex replacements.
    re_subs = [(re.escape(k), v) for k, v in repls]
    re_subs += add_re_subs
    assert len(re_subs) == len(repls) + len(add_re_subs)
           
    return re_subs
    
def get_re_subs_for_pair(dest_key, dest_val):    
    '''Gets regular expression pairs for use in re.subn (called by re_sub_all). 
        For use when a pair of source documents, is copied and the key and 
        value primatives are replaced.'''
    tds = get_typedefs()
    src_key = tds.get("long")
    src_val = tds.get("double")
    
    # Pairs (as tuples) of strings which should be replace exactly.
    repls = []
        
    # Prepend some replacements for generics in the unit tests. 
    if dest_key == "Int":
        repls += [("<Long,", "<Integer,")]
    if dest_val == "Int":
        repls += [("Double>", "Integer>")] #TODO: should this have parens???
    # Conditional replacements
    if dest_val.is_integral:
        repls += [(", double delta", ""),
                  (", delta", ""),
                  (", double zeroThreshold", ""),
                  (", zeroThreshold", ""),
                  (", 1e-13", ""),
                  ]
    
    # Special cases to always use.
    repls += [#("getIntIndexArray", "getIndexArray"),
             ("int serialVersionUID", "long serialVersionUID"),
             # TODO: The MIN_VALUEs and MAX_VALUEs are not being sorted properly.
             ("Long.POSITIVE_INFINITY", "9223372036854775806l"),
             ("Long.NEGATIVE_INFINITY", "-9223372036854775806l"),
             ("Int.POSITIVE_INFINITY", "2147483646"),
             ("Int.NEGATIVE_INFINITY", "-2147483646"),
             ]
    
    # Add the primary replacements.
    repls += get_typedef_repls(src_key, dest_key)
    repls += get_typedef_repls(src_val, dest_val)
        
    # Additional regular expression replacements to be concatenated at the end.
    add_re_subs = []
    
    # Excluded code.
    if dest_val.is_integral:
        add_re_subs += [
            # Remove everything between the markers.
            (r"START EXCLUDE ILV (\S+).*END EXCLUDE \1", ""),
            ]
    else:
        add_re_subs += [
            # Just remove the markers.
            (r"(START EXCLUDE ILV (\S+)|END EXCLUDE (\S+))", ""),
            ]
        
    if dest_val.prim == "int":
        add_re_subs += [
            # Remove everything between the markers.
            (r"START EXCLUDE IV (\S+).*END EXCLUDE \1", ""),
            ]
    else:
        add_re_subs += [
            # Just remove the markers.
            (r"(START EXCLUDE IV (\S+)|END EXCLUDE (\S+))", ""),
            ]
        
    if dest_key.prim == "int" and dest_val.prim == "int":
        add_re_subs += [
            # Remove everything between the markers.
            (r"START EXCLUDE IK IV (\S+).*END EXCLUDE \1", ""),
            ]
    else:
        add_re_subs += [
            # Just remove the markers.
            (r"(START EXCLUDE IK IV (\S+)|END EXCLUDE (\S+))", ""),
            ]
        
    # Other regex replacements
    add_re_subs += [(r"SafeCast\.safeIntToInt\(([^\)]+)\)", r"\1"),
               ]
    
    # Convert repls to regex replacements.
    re_subs = [(re.escape(k), v) for k, v in repls]
    re_subs += add_re_subs
    assert len(re_subs) == len(repls) + len(add_re_subs)
           
    return re_subs

def copy_and_sub(re_subs, src_files):
    dest_files = map(lambda f : re_sub_all(re_subs, f), src_files)
    for sf, df in zip(src_files, dest_files):
        assert sf != df
        print "Current destination file:", df
        dest_str = file_to_str(sf)
        dest_str = re_sub_all(re_subs, dest_str)
        str_to_file(dest_str, df)
        
def copy_single(src_prim, dest_prim, src_files):
    '''Copies a class defined for a source primitive to a new class with the destination primitive.
    '''
    re_subs = get_re_subs_for_single(src_prim, dest_prim)
    copy_and_sub(re_subs, src_files)

def copy_pair(dest_key, dest_val):
    '''Copies a set of classes defined with key=Long, value=Double primitives to a new class
        using key=dest_key and value=dest_val.
    '''
    re_subs = get_re_subs_for_pair(dest_key, dest_val)
    
    main_classes = [
                 "edu.jhu.prim.map.LongDoubleMap",
                 "edu.jhu.prim.map.LongDoubleEntry",
                 "edu.jhu.prim.map.LongDoubleSortedMap",
                 "edu.jhu.prim.map.LongDoubleHashMap",
                 "edu.jhu.prim.sort.LongDoubleSort",
                 # "edu.jhu.prim.set.LongHashSet",
                 "edu.jhu.prim.vector.LongDoubleVector",
                 "edu.jhu.prim.vector.LongDoubleSortedVector",
                 "edu.jhu.prim.vector.LongDoubleHashVector",
                 "edu.jhu.prim.vector.LongDoubleDenseVector",
                 "edu.jhu.prim.vector.LongDoubleVectorSlice",
                 ]
    main_java = os.path.join("src", "main", "java")
    src_files = [os.path.join(main_java, c.replace(".", "/") + ".java") for c in main_classes]

    test_classes = [
                 "edu.jhu.prim.map.LongDoubleSortedMapTest",
                 "edu.jhu.prim.map.LongDoubleHashMapTest",
                 "edu.jhu.prim.sort.LongDoubleSortTest",
                 "edu.jhu.prim.vector.LongDoubleSortedVectorTest",
                 "edu.jhu.prim.vector.LongDoubleHashVectorTest",
                 "edu.jhu.prim.vector.LongDoubleDenseVectorTest",
                 "edu.jhu.prim.vector.LongDoubleVectorSliceTest",
                 "edu.jhu.prim.vector.AbstractLongDoubleVectorTest",
                 ]
    test_java = os.path.join("src", "test", "java")
    src_files += [os.path.join(test_java, c.replace(".", "/") + ".java") for c in test_classes]

    copy_and_sub(re_subs, src_files)

if __name__ == "__main__":
    tds = get_typedefs()
    copy_pair(tds.get("int"), tds.get("double"))
    copy_pair(tds.get("int"), tds.get("long"))
    #copy_pair(tds.get("short"), tds.get("int"))
    
    #TODO: IntInt/LongInt sort of works, but requires the removal of some duplicate methods/constructors:
    copy_pair(tds.get("long"), tds.get("int"))
    copy_pair(tds.get("int"), tds.get("int"))

