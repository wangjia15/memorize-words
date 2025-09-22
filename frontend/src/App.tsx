import { useState } from 'react'
import { Button } from './components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './components/ui/card'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800">
      <div className="container mx-auto px-4 py-8">
        <header className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
            Memorize Words
          </h1>
          <p className="text-xl text-gray-600 dark:text-gray-300">
            Expand your vocabulary one word at a time
          </p>
        </header>

        <main className="max-w-4xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <Card>
              <CardHeader>
                <CardTitle>Today's Words</CardTitle>
                <CardDescription>
                  Practice words scheduled for today
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-2xl font-bold text-primary">0 words</p>
                <Button className="mt-4" variant="outline">
                  Start Practice
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Progress</CardTitle>
                <CardDescription>
                  Track your learning journey
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-2xl font-bold text-primary">0 words learned</p>
                <div className="mt-4">
                  <div className="w-full bg-gray-200 rounded-full h-2.5">
                    <div className="bg-primary h-2.5 rounded-full" style={{ width: '0%' }}></div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>
                Manage your vocabulary learning
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-4">
                <Button>Add New Words</Button>
                <Button variant="outline">View Dictionary</Button>
                <Button variant="outline">Settings</Button>
              </div>
            </CardContent>
          </Card>

          <div className="mt-8 text-center">
            <Card className="inline-block">
              <CardContent className="pt-6">
                <p className="text-sm text-muted-foreground mb-4">
                  Demo counter to test React state management
                </p>
                <div className="flex items-center gap-4">
                  <Button
                    onClick={() => setCount((count) => count - 1)}
                    variant="outline"
                    size="sm"
                  >
                    -
                  </Button>
                  <span className="text-lg font-semibold">{count}</span>
                  <Button
                    onClick={() => setCount((count) => count + 1)}
                    variant="outline"
                    size="sm"
                  >
                    +
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </main>

        <footer className="text-center mt-12 text-gray-500 dark:text-gray-400">
          <p>Built with React, TypeScript, and shadcn/ui</p>
        </footer>
      </div>
    </div>
  )
}

export default App