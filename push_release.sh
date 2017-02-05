BRANCH="v1.1"

# Are we on the right branch?
if [ "$TRAVIS_BRANCH" = "$BRANCH" ]; then
  
  # Is this not a Pull Request?
  if [ "$TRAVIS_PULL_REQUEST" = false ]; then
    
    # Is this not a build which was triggered by setting a new tag?
    if [ -z "$TRAVIS_TAG" ]; then
      echo -e "Starting to push release.\n"
      
      git config --global user.email "builds@travis-ci.com"
      git config --global user.name "Travis CI"
      
      export GIT_TAG=" $TRAVIS_BRANCH.$TRAVIS_BUILD_NUMBER-SNAPSHOT" 
      git tag $GIT_TAG -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"
      git push -q https://$GITHUBKEY@github.com/TryHardDood/myZone $GIT_TAG
      
      git fetch origin
      
      echo -e "Done pushing the release.\n"
    fi
  fi
fi
