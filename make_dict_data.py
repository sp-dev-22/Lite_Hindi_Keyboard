import json, re

# Let's read the dictionary entries provided by user and construct a comprehensive map:
# roman -> list of devanagari words sorted by rank/frequency
# We will embed high-frequency and all user words.
