#!/usr/local/bin/python

import re
import sys
import os
import getopt
import math
import tempfile
import stat
import subprocess
from optparse import OptionParser
from experiments.run_srl import SrlExpParams
from glob import glob
from experiments.core.util import get_all_following, get_following, get_time, get_following_literal,\
    to_str, to_int, get_group1, head, get_match
from experiments.core.scrape import Scraper
from experiments.core.util import tail
from experiments import scrape_statuses
import shlex
from experiments.core import scrape

def get_root_dir():
    scripts_dir =  os.path.abspath(sys.path[0])
    root_dir =  os.path.dirname(os.path.dirname(scripts_dir))
    print "Using root_dir: " + root_dir
    return root_dir;

class SrlScraper(Scraper):
    
    def __init__(self, options):
        Scraper.__init__(self, options)
        self.root_dir = os.path.abspath(get_root_dir())

    def get_exp_params_instance(self):
        return SrlExpParams()
    
    def get_column_order(self, exp_list):
        hs = ""
        hs += "exp_dir tagger_parser trainMaxNumSentences trainMaxSentenceLength "
        hs += " roleStructure linkVarType feature_set "
        hs += " optimizer l2variance initialLr "
        order = hs.split()
        # Add the columns from the evaluation. 
        for ns in [False, True]:
            for lu in ["Labeled", "Unlabeled"]:
                for fpr in ["attachment-score", "F1", "precision", "recall"]:
                    for tt in ["train", "test"]:
                        if ns:
                            order.append("-".join([tt+"-no-sense", lu, fpr]))
                        else:
                            order.append("-".join([tt, lu, fpr]))
        return order        
    
    def scrape_exp(self, exp, exp_dir, stdout_file):
        if not os.path.exists(stdout_file):
            return
        
        stdout_lines = self.read_stdout_lines(stdout_file)
        
        _, _, elapsed = get_time(stdout_lines)
        exp.update(elapsed = elapsed)
        #exp.update(hours = to_int(get_group1(stdout_lines, "^(\d+) +[A-Z][A-Z]+", -1)) / 1000.0 / 60.0 / 60.0)
        
        exp.update(numSents = to_int(get_following_literal(stdout_lines, "Num train sentences: ")))
        exp.update(numWords = to_int(get_following_literal(stdout_lines, "Num train tokens: ")))
        exp.update(numRoles = to_int(get_following_literal(stdout_lines, "Num known roles: ")))

        exp.update(trainAccuracy = get_following_literal(stdout_lines, "Accuracy on train: ", -1))
        exp.update(trainLogLikelihood = get_following_literal(stdout_lines, "Marginal log-likelihood: ", -1))
        exp.update(testAccuracy = get_following_literal(stdout_lines, "Accuracy on test: ", -1))
        # TODO: exp.update(testLogLikelihood = get_following_literal(stdout_lines, "LogLikelihood on test: ", -1))
        exp.update(finalObjValue = get_following_literal(stdout_lines, "Final objective value: ", -1))
            
        self.get_eval(exp, exp_dir, "train")
        self.get_eval(exp, exp_dir, "train-no-sense")
        self.get_eval(exp, exp_dir, "test")
        self.get_eval(exp, exp_dir, "test-no-sense")
    
    def get_eval(self, exp, exp_dir, data_name):
        eval_file = os.path.join(exp_dir, data_name + "-eval.out")
        if not os.path.exists(eval_file):
            return
        lines = self.read_stdout_lines(eval_file)
        # SYNTACTIC SCORES
        exp.set(data_name + "-Labeled-attachment-score", get_following(lines, "Labeled   attachment score:[^=]+\s*="))
        exp.set(data_name + "-Unlabeled-attachment-score", get_following(lines, "Unlabeled attachment score:[^=]+\s*="))
        # SEMANTIC SCORES
        exp.set(data_name + "-Labeled-F1",        get_following(lines, "Labeled F1:\s+"))
        exp.set(data_name + "-Labeled-precision", get_following(lines, "Labeled precision:[^=]+\s*="))
        exp.set(data_name + "-Labeled-recall",    get_following(lines, "Labeled recall:[^=]+\s*="))
        exp.set(data_name + "-Unlabeled-F1",        get_following(lines, "Unlabeled F1:\s+"))
        exp.set(data_name + "-Unlabeled-precision", get_following(lines, "Unlabeled precision:[^=]+\s*="))
        exp.set(data_name + "-Unlabeled-recall",    get_following(lines, "Unlabeled recall:[^=]+\s*="))
            
if __name__ == "__main__":
    usage = "%prog [top_dir...]"

    parser = OptionParser(usage=usage)
    parser.add_option('-q', '--queue', help="Which SGE queue to use")
    parser.add_option('-f', '--fast', action="store_true", help="Run a fast version")
    scrape.add_options(parser)
    (options, args) = parser.parse_args(sys.argv)

    if len(args) < 2:
        parser.print_help()
        sys.exit(1)
    
    scraper = SrlScraper(options)
    for top_dir in args[1:]:
        scraper.scrape(top_dir)
