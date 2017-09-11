"""Spelling Corrector.

Copyright 2007 Peter Norvig. 
Open source code under MIT license: http://www.opensource.org/licenses/mit-license.php
"""

import re, collections
import sys
import pdb
import socket
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
from optparse import OptionParser

def words(text): return re.findall('[a-z]+', text.lower()) 

def train(features):
    model = collections.defaultdict(lambda: 1)
    for f in features:
        model[f] += 1
    return model

NWORDS = train(words(file('/home/ec2-user/spellcheck/train-data.txt').read()))

alphabet = 'abcdefghijklmnopqrstuvwxyz'

def edits1(word):
    s = [(word[:i], word[i:]) for i in range(len(word) + 1)]
    deletes    = [a + b[1:] for a, b in s if b]
    transposes = [a + b[1] + b[0] + b[2:] for a, b in s if len(b)>1]
    replaces   = [a + c + b[1:] for a, b in s for c in alphabet if b]
    inserts    = [a + c + b     for a, b in s for c in alphabet]
    return set(deletes + transposes + replaces + inserts)

def known_edits2(word):
    return set(e2 for e1 in edits1(word) for e2 in edits1(e1) if e2 in NWORDS)

def known(words): return set(w for w in words if w in NWORDS)

def correct(word):
    candidates = known([word]) or known(edits1(word)) or known_edits2(word) or [word]
    if len(candidates) > 5:
        th = 5
    else:
        th = len(candidates)
    return sorted(candidates, key=NWORDS.get,reverse=True)[:th]

def server():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(("localhost", 9000))
    s.listen(1)
    while True:
        print("Server waiting to accept")
        conn, addr = s.accept()
        data = conn.recv(5000)
        if data == "shutdown spell check":
            conn.close()
            s.close()
            break
            
        print("recieved data " + data)
        for word in data.split():
            conn.send(' '.join(correct(word)))

        conn.close()

if __name__ == '__main__':
    server()
    
