# Test of function declarations

# Function read in source mode

unset _A
foo()
{
  echo foo: args are $*
  _A=foo
}

function bar()
{
  echo bar: args are $*
  _A=bar
}

unset _A
foo arg1 arg2 arg3
echo after foo _A is $_A
unset _A
bar arg1 arg2
echo after bar _A is $_A